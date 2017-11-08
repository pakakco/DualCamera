package android.pakaco.glvideorecorder.pip.pipwrapping;

/**
 * Created by Administrator on 2017/6/9.
 */
public class PIPCustomization {
    private static final String TAG = PIPCustomization.class.getSimpleName();
    public static final String MAIN_CAMERA = "main_camera";
    public static final String SUB_CAMERA = "sub_camera";
    // scale
    public static final float TOP_GRAPHIC_MAX_SCALE_VALUE = 1.4f;
    public static final float TOP_GRAPHIC_MIN_SCALE_VALUE = 0.6f;
    // rotate
    public static final float TOP_GRAPHIC_MAX_ROTATE_VALUE = 180f;
    // top graphic edge, default is min(width, height) / 2
    public static final float TOP_GRAPHIC_DEFAULT_EDGE_RELATIVE_VALUE = 1f / 2;
    // edit button edge, default is min(width, height) / 10
    public static final int TOP_GRAPHIC_EDIT_BUTTON_RELATIVE_VALUE = 10;
    public static final float TOP_GRAPHIC_LEFT_TOP_RELATIVE_VALUE =
            TOP_GRAPHIC_DEFAULT_EDGE_RELATIVE_VALUE
                    - 1f / TOP_GRAPHIC_EDIT_BUTTON_RELATIVE_VALUE;
    // top graphic crop preview position
    public static final float TOP_GRAPHIC_CROP_RELATIVE_POSITION_VALUE = 3f / 4;
    // which camera enable FD, default is main camera support fd
    public static final String ENABLE_FACE_DETECTION = MAIN_CAMERA;
    // when take picture, whether sub camera need mirror
    public static final boolean SUB_CAMERA_NEED_HORIZONTAL_FLIP = true;
    private PIPCustomization() {
    }
    public static boolean isMainCameraEnableFD() {
        boolean enable = false;
        enable = ENABLE_FACE_DETECTION.endsWith(MAIN_CAMERA);
        return enable;
    }
}
