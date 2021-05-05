package usage;

public class UserInfo {
    private String username;
    private int prestigeValue,point;

    public UserInfo(String username, int prestigeValue, int point) {
        this.username = username;
        this.prestigeValue = prestigeValue;
        this.point = point;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getPrestigeValue() {
        return prestigeValue;
    }

    public void setPrestigeValue(int prestigeValue) {
        this.prestigeValue = prestigeValue;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }
}
