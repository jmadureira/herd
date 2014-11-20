package io.herd.eventbus;

/**
 * Wrapper envelope for the data being passed.
 * 
 * @author joaomadureira
 *
 */
class Envelope {

    private Object data;

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
