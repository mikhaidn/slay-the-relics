package str_exporter.game_state;

public class Transform {
    public float offsetX;
    public float offsetY;
    public float scaleX;
    public float scaleY;

    public Transform(float offsetX, float offsetY, float scaleX, float scaleY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
    }

    public Transform() {
        this(0, 0, 100, 100);
    }
}
