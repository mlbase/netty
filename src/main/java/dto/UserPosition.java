package dto;

public class UserPosition {
    private String userId;
    private String positionXY;

    public UserPosition() {
    }


    public String getUserId() {
        return userId;
    }

    public String getPositionXY() {
        return positionXY;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPositionXY(String positionXY) {
        this.positionXY = positionXY;
    }

    @Override
    public String toString() {
        return "UserPosition{" +
                "userId='" + userId + '\'' +
                ", positionXY='" + positionXY + '\'' +
                '}';
    }
}
