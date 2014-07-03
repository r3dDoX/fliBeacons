package ch.fork.flibeacons.events;

/**
 * Created by riba on 03.07.2014.
 */
public class PictureTakenEvent {

    private String base64Image;

    public PictureTakenEvent(String base64Image){
        this.base64Image = base64Image;
    }

    public String getBase64Image(){
        return base64Image;
    }
}
