package it.unibs.pajc;

import javax.swing.event.*;

public class BaseModel {
    protected EventListenerList list = new EventListenerList();

    public void addChangeListener(ChangeListener l) {
        list.add(ChangeListener.class, l);
    }

    public void removeChangeListener(ChangeListener l) {
        list.remove(ChangeListener.class, l);
    }

    protected void fireChangeListener() {
        ChangeEvent e = new ChangeEvent(this);

        Object[] listeners = list.getListenerList();
        for(int i = listeners.length - 2; i>=0; i-=2 ) {
            if(listeners[i] == ChangeListener.class) {
                ((ChangeListener)listeners[i+1]).stateChanged(e);
            }
        }
    }
}
