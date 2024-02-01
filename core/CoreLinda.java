package core;


import utils.Callback;
import utils.Linda;
import utils.Tuple;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Storage Core for parallel writing/reading of a tuplespace.
 * 
 * @author Sylvain Crouzet
 */
public class CoreLinda implements Linda {

    /** Tuple-space. */
    private ArrayList<Tuple> storage;
    
    /** Lock to access the Tuple-space */
    private ReadWriteLock storageLock;
    
    /** Lock to access Read callbacks*/
    private ReentrantLock readCallbackLock;
    /** Lock to access Take callbacks */
    private ReentrantLock takeCallbackLock;

    /** Lock to access Read callbacks*/
    private ReentrantLock waitReadLock;
    /** Lock to access Take callbacks */
    private ReentrantLock waitTakeLock;


    /* Lists of Callbacks */
    private HashMap<Tuple, ArrayList<Callback>> mapTakeCallbacks;
    private HashMap<Tuple, ArrayList<Callback>> mapReadCallbacks;

    /* Conditions for blocking operations. */
    private HashMap<Tuple, Condition> waitingRead;
    private HashMap<Tuple, Condition> waitingTake;

    public CoreLinda() {
        storage = new ArrayList<Tuple>();
        // fairness true ?
        storageLock = new ReentrantReadWriteLock();
        waitingRead = new HashMap<Tuple, Condition>();
        waitingTake = new HashMap<Tuple, Condition>();
        mapTakeCallbacks = new HashMap<Tuple, ArrayList<Callback>>();
        mapReadCallbacks = new HashMap<Tuple, ArrayList<Callback>>();
        readCallbackLock = new ReentrantLock();
        takeCallbackLock = new ReentrantLock();
        waitReadLock = new ReentrantLock();
        waitTakeLock = new ReentrantLock();
    }


    public void write(Tuple t) {
        
        /* Check Callbacks Read */
        readCallbackLock.lock();
        try {
            for (Iterator<Tuple> iterator = mapReadCallbacks.keySet().iterator(); iterator.hasNext();) {
                Tuple template = iterator.next();
                if (t.matches(template)) {
                    for (Callback cb : mapReadCallbacks.get(template)) {
                        cb.call(t.deepclone());
                    }
                    iterator.remove();
                }
            }
        } finally {
            readCallbackLock.unlock();
        }

        /* Check Callbacks Take */
        takeCallbackLock.lock();
        try {
            for (Iterator<Tuple> iterator = mapTakeCallbacks.keySet().iterator(); iterator.hasNext();) {
                Tuple template = iterator.next();
                if (t.matches(template)) {
                    List<Callback> l = mapTakeCallbacks.get(template);
                    l.removeFirst().call(t.deepclone());
                    if (l.isEmpty())
                        iterator.remove();
                    return;
                }
            }
        } finally {
            takeCallbackLock.unlock();
        }

        /** add to storage */
        storageLock.writeLock().lock();
        try {
            storage.add(t.deepclone());
        } finally {
            storageLock.writeLock().unlock();
        }

        /** Signals to threads waiting to read this tuple. */
        waitReadLock.lock();
        try {
            for (Iterator<Tuple> iterator = waitingRead.keySet().iterator(); iterator.hasNext();) {
                Tuple template = iterator.next();
                if (t.matches(template)) {
                    waitingRead.get(template).signalAll();
                    iterator.remove();
                }
            }
        } finally {
            waitReadLock.unlock();
        }

        /** Signals to a thread waiting to Take this tuple. */
        waitTakeLock.lock();
        try {
            for (Iterator<Tuple> iterator = waitingTake.keySet().iterator(); iterator.hasNext();) {
                Tuple template = iterator.next();
                if (t.matches(template)) {
                    waitingTake.get(template).signalAll();
                    iterator.remove();
                    break;
                }
            }
        } finally {
            waitTakeLock.unlock();
        }
    }

    
    public Tuple take(Tuple template){
        Tuple result = null;
        boolean found = false;
        while (!found) {
            storageLock.writeLock().lock();
            try {
                for (Iterator<Tuple> iterator = storage.iterator(); iterator.hasNext();) {
                    Tuple t = iterator.next();
                    if (t.matches(template)) {
                        result = t;
                        iterator.remove();
                        found = true;
                        break;
                    }
                }
            } finally {
                storageLock.writeLock().unlock();
            }
            /* Wait for a write. */
            if (!found) {
                waitTakeLock.lock();
                try {
                    Condition cond = waitingTake.get(template);
                    if (cond == null) {
                        cond = waitTakeLock.newCondition();
                    }
                    waitingTake.put(template.deepclone(), cond);
                    cond.await();
                } catch (InterruptedException e) {
                    found = true;
                } finally {
                    waitTakeLock.unlock();
                }
            }
        }
        return result;
    }
        
        
    public Tuple read(Tuple template){

        Tuple result = null;
        boolean found = false;
        while (!found) {
            storageLock.readLock().lock();
            try {
                for (Iterator<Tuple> iterator = storage.iterator(); iterator.hasNext();) {
                    Tuple t = iterator.next();
                    if (t.matches(template)) {
                        result = t.deepclone();
                        found = true;
                        break;
                    }
                }
            } finally {
                storageLock.readLock().unlock();
            }
            /* Wait for a put. */
            if (!found) {
                waitReadLock.lock();
                try {
                    Condition cond = waitingRead.get(template);
                    if (cond == null) {
                        cond = waitReadLock.newCondition();
                    }
                    waitingRead.put(template.deepclone(), cond);
                    cond.await();
                } catch (InterruptedException e) {
                    found = true;
                } finally {
                    waitReadLock.unlock();
                }
            }
        }
        return result;
    }

    public Tuple tryTake(Tuple template) {
        Tuple result = null;
        storageLock.writeLock().lock();
        try {
            for (Tuple t : storage) {
                if (t.matches(template)) {
                    result = t;
                    break;
                }
            }
            if (result != null)
                storage.remove(result);
        } finally {
            storageLock.writeLock().unlock();
        }
        return result;
    }


    public Tuple tryRead(Tuple template){


        Tuple result = null;
        storageLock.readLock().lock();
        try {
            for (Tuple t : storage) {
                if (t.matches(template)) {
                    result = t.deepclone();
                    break;
                }
            }
        } finally {
            storageLock.readLock().unlock();
        }
        return result;
    }



    public Collection<Tuple> takeAll(Tuple template){
        ArrayList<Tuple> aList = new ArrayList<Tuple>();

        storageLock.writeLock().lock();
        try {
            Tuple t;
            for (Iterator<Tuple> iterator = storage.iterator(); iterator.hasNext();) {
                t = iterator.next();
                if (t.matches(template)) {
                    aList.add(t);
                    iterator.remove();
                }
            }
        } finally {
            storageLock.writeLock().unlock();
        }
        return aList;
    }


    public Collection<Tuple> readAll(Tuple template){
        ArrayList<Tuple> aList = new ArrayList<Tuple>();

        storageLock.readLock().lock();
        try {
            for (Tuple t : storage) {
                if (t.matches(template)) {
                    aList.add(t.deepclone());
                }
            }
        } finally {
            storageLock.readLock().unlock();
        }
        return aList;
    }



    public void eventRegister(eventMode mode, eventTiming timing, Tuple template, Callback callback){

        if (mode == eventMode.TAKE) { /** TAKE */

            Tuple t = null;
            if (timing == eventTiming.IMMEDIATE)
                t = tryTake(template);

            if (t == null) {
                /** Registers callback. */
                takeCallbackLock.lock();
                try {
                    ArrayList<Callback> cbList = mapTakeCallbacks.get(template);
                    if (cbList == null) {
                        cbList = new ArrayList<Callback>();
                        cbList.add(callback);
                        mapTakeCallbacks.put(template.deepclone(), cbList);
                    } else {
                        cbList.add(callback);
                    }
                } finally {
                    takeCallbackLock.unlock();
                }
            }

        } else { /** READ */
            Tuple t = null;
            if (timing == eventTiming.IMMEDIATE)
                t = tryRead(template);

            if (t == null) {
                /** Registers callback. */
                readCallbackLock.lock();
                try {

                    ArrayList<Callback> cbList = mapReadCallbacks.get(template);
                    if (cbList == null) {
                        cbList = new ArrayList<Callback>();
                        cbList.add(callback);
                        mapReadCallbacks.put(template.deepclone(), cbList);
                    } else {
                        cbList.add(callback);
                    }
                } finally {
                    readCallbackLock.unlock();
                }
            }
        }
    }

    public void debug(String s){
        System.out.println(s);
    }


}