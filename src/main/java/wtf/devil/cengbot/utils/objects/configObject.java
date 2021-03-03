package wtf.devil.cengbot.utils.objects;

public class configObject {

    private String token;



    public configObject(String discordToken) {
        this.token = discordToken;
    }

    public String getToken() {
        return token;
    }
}
