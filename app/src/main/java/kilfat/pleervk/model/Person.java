package kilfat.pleervk.model;

/**
 */
public class Person {
    private String firstName;
    private String lastName;
    private int id;
    private int audioCount;
    public Person(){
    }
    public Person(String firstName, String lastName, int id, int audioCount){
        this.firstName=firstName;
        this.lastName=lastName;
        this.id=id;
        this.audioCount=audioCount;
    }
    public String getFirstLastName() {
        return firstName+" "+lastName;
    }
    public int getId(){
        return id;
    }
    public int getAudioCount(){
        return audioCount;
    }
    public void setAudioCount(int audioCount){
        this.audioCount=audioCount;
    }
}
