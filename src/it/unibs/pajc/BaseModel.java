package it.unibs.pajc;

import javax.swing.event.*;

public class BaseModel {
    protected EventListenerList list = new EventListenerList();

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
