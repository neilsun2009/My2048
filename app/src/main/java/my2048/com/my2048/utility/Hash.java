package my2048.com.my2048.utility;

import android.view.View;

import my2048.com.my2048.R;

/**
 * Created by lenovo on 2016/3/17.
 */
public class Hash {
    public final static int[] ORI_IMAGE_RESOURCE = {R.id.ori_image_01, R.id.ori_image_02, R.id.ori_image_03,
            R.id.ori_image_04, R.id.ori_image_05, R.id.ori_image_06, R.id.ori_image_07, R.id.ori_image_08,
            R.id.ori_image_09, R.id.ori_image_10, R.id.ori_image_11, R.id.ori_image_12, R.id.ori_image_13,
            R.id.ori_image_14, R.id.ori_image_15, R.id.ori_image_16};
    public final static int[] BLOCK_RESOURCE = {R.drawable.block_0, R.drawable.block_2, R.drawable.block_4,
            R.drawable.block_8, R.drawable.block_16, R.drawable.block_32, R.drawable.block_64, R.drawable.block_128,
            R.drawable.block_256, R.drawable.block_512, R.drawable.block_1024, R.drawable.block_2048,
            R.drawable.block_4096, R.drawable.block_8192, R.drawable.block_16384, R.drawable.block_32768};
    public static int blockHash(int x) {
        switch (x) {
            case 0:
                return 0;
            case 2:
                return 1;
            case 4:
                return 2;
            case 8:
                return 3;
            case 16:
                return 4;
            case 32:
                return 5;
            case 64:
                return 6;
            case 128:
                return 7;
            case 256:
                return 8;
            case 512:
                return 9;
            case 1024:
                return 10;
            case 2048:
                return 11;
            case 4096:
                return 12;
            case 8192:
                return 13;
            case 16384:
                return 14;
            case 32768:
                return 15;
            default:
                return 0;
        }
    }
    public static int[][] imageLocation;
    public final static int[][] MOVE_SEQUENCE = {{0,4,8,12,1,5,9,13,2,6,10,14,3,7,11,15,-1},
            {3,7,11,15,2,6,10,14,1,5,9,13,0,4,8,12,1},
            {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,-4},
            {12,13,14,15,8,9,10,11,4,5,6,7,0,1,2,3,4}};
    public final static int[][] MOVE_BOUNDRY = {{0,1,2,3,0,1,2,3,0,1,2,3,0,1,2,3},
            {3,2,1,0,3,2,1,0,3,2,1,0,3,2,1,0},{0,0,0,0,1,1,1,1,2,2,2,2,3,3,3,3},
            {3,3,3,3,2,2,2,2,1,1,1,1,0,0,0,0}};
}
