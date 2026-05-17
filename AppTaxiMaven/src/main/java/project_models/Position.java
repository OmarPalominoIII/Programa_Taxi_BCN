package project_models;

public class Position {
    private double latitude;
    private double longitude;

    public Position(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude()       { return latitude; }
    public double getLongitude()    { return longitude; }

    public void setRow(double latitude)       { this.latitude = latitude; }
    public void setColumn(double longitude) { this.longitude = longitude; }

    @Override
    public String toString() {
        return "Position | Latitude: " + latitude + ", Longitude: " + longitude;
    }
}
