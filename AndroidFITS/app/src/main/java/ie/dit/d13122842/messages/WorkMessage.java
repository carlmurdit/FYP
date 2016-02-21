package ie.dit.d13122842.messages;

public class WorkMessage {
    private String cID;
    private String wID;
    private String filename;
    private int planes;
    private Long deliveryTag; //message tag, used to ack

    public WorkMessage(Object cID, Object wID, Object filename, Object planes, Long deliveryTag) {
        this.cID = (String) cID;
        this.wID = (String) wID;
        this.filename = (String) filename;
        this.planes = (int)(long) planes;
        this.deliveryTag = deliveryTag;
    }

    public String getcID() {
        return cID;
    }

    public void setcID(String cID) {
        this.cID = cID;
    }

    public String getwID() {
        return wID;
    }

    public void setwID(String wID) {
        this.wID = wID;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getPlanes() {
        return planes;
    }

    public void setPlanes(int planes) {
        this.planes = planes;
    }

    public Long getDeliveryTag() {
        return deliveryTag;
    }

    public void setDeliveryTag(Long deliveryTag) {
        this.deliveryTag = deliveryTag;
    }

    public String toString() {
        return String.format("cID=%s\nwID=%s\nfilename=%s\nplanes=%d\ndeliveryTag=%s",
                cID , wID, filename, planes, deliveryTag);
    }


}