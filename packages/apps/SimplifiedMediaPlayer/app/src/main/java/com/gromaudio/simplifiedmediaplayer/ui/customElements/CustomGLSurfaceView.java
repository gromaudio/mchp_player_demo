package com.gromaudio.simplifiedmediaplayer.ui.customElements;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;

import com.gromaudio.simplifiedmediaplayer.R;
import com.gromaudio.utils.Logger;
import com.gromaudio.utils.Utils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.gromaudio.simplifiedmediaplayer.ui.customElements.CustomGLSurfaceView.ANIMATION.ANIMATION_FROM_LEFT_TO_RIGHT;
import static com.gromaudio.simplifiedmediaplayer.ui.customElements.CustomGLSurfaceView.ANIMATION.ANIMATION_FROM_RIGHT_TO_LEFT;
import static com.gromaudio.simplifiedmediaplayer.ui.customElements.CustomGLSurfaceView.ANIMATION.ANIMATION_WITHOUT;


public class CustomGLSurfaceView extends GLSurfaceView implements GLSurfaceView.Renderer {

    public static final String TAG = "CustomGLSurfaceView";
    private static final boolean DEBUG = true;//local debug flag

    private static final int INVALID_BITMAP_IDENTIFIER = Integer.MIN_VALUE;

    public static final int NUMBER_OF_UPDATES_SURFACE = 10;

    public enum ANIMATION {
        ANIMATION_WITHOUT,
        ANIMATION_FROM_RIGHT_TO_LEFT,
        ANIMATION_FROM_LEFT_TO_RIGHT
    }

    public static final String SQUARE_TYPE_WIDTH = "width";
    public static final String SQUARE_TYPE_HEIGHT = "height";
    public static final String SQUARE_TYPE_DEFAULT = "not_square";


    @NonNull
    private static final int[] CLEANUP_TEXTURE_INDICES = new int[1];
    //Cleanup list
    @NonNull
    private final List<Integer> mCleanup = new ArrayList<>();
    //Textures that are bound
    @NonNull
    private final List<CoverTexture> mTexturesBound = new ArrayList<>();


    private Canvas mCanvas;
    //Default texture
    @Nullable
    private CoverTexture mDefaultTexture;
    private MeshSquare mMesh;

    private int mCountCaverRendering = 0;
    private boolean mRenderable = false;

    //The [-1,1] measure of shift from current song if abs(shift) > 0.2 means
    //that when we touch up finger, we switch track to next prev
    private float mTouchShift;
    private boolean mJustInit = true;


    private String mSquareType = SQUARE_TYPE_DEFAULT;
    @NonNull
    private ANIMATION mAnimationState = ANIMATION_WITHOUT;




    private int mBitmapIdentifier;
    @Nullable
    private Bitmap mBitmap;

    //Origin track id
    @NonNull
    private final AtomicInteger mOriginBitmapIdentifier = new AtomicInteger(INVALID_BITMAP_IDENTIFIER);
    //Shift index
    @NonNull
    private final AtomicInteger mShiftBitmapIdentifier = new AtomicInteger(INVALID_BITMAP_IDENTIFIER);

    /*@Nullable
    private IProgressIndicator mProgressIndicatorListener;*/


    public CustomGLSurfaceView(Context context) {
        super(context);

        if (!isInEditMode()) {
            initView(context, null);
        }
    }

    public CustomGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        if (!isInEditMode()) {
            initView(context, attrs);
        }
    }


    private void initView(Context context, AttributeSet attrs) {

        final TypedArray ta = context
            .obtainStyledAttributes(attrs, R.styleable.CustomGLSurfaceView);
        if (ta != null) {
            mSquareType = ta.getString(R.styleable.CustomGLSurfaceView_squareType);
            if (TextUtils.isEmpty(mSquareType)) {
                mSquareType = SQUARE_TYPE_DEFAULT;
            }
            ta.recycle();
        }

        mCanvas = new Canvas();

        setRenderer(this);
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mSquareType.equals(SQUARE_TYPE_WIDTH)) {
            widthMeasureSpec = heightMeasureSpec = getMeasuredWidth();
        } else if (mSquareType.equals(SQUARE_TYPE_HEIGHT)) {
            widthMeasureSpec = heightMeasureSpec = getMeasuredHeight();
        }

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (DEBUG)
            Logger.i(TAG, "onResume(), BitmapIdentifier= " + mBitmapIdentifier);

        setJustInit();
        setRenderable(true);
    }

    @Override
    public void onPause() {
        if (DEBUG)
            Logger.i(TAG, "onPause(), BitmapIdentifier= " + mBitmapIdentifier);

        super.onPause();
    }


    public void setRenderable(boolean renderable) {
        if (DEBUG) Logger.d(TAG, "setRenderable() renderable= " + renderable);

        mRenderable = renderable;
        mCountCaverRendering = 0;
        requestRender();
    }

    public void setJustInit() {
        mJustInit = true;
    }


    public void setCover(@Nullable Bitmap bitmap,
                         int bitmapIdentifier,
                         @NonNull ANIMATION animation) {
        if (DEBUG) {
            Logger.d(TAG, "setCaver, BitmapIdentifier= " + bitmapIdentifier);
        }

        mBitmap = bitmap;
        mBitmapIdentifier = bitmapIdentifier;
        mAnimationState = animation;
        setRenderable(true);
    }


    /**
     * calculates power of 2 for bitmaps
     *
     * @param size size
     * @return int
     */
    static int nextPOT(final int size) {
        int psize = 2;
        while (psize < size) {
            psize <<= 1;
        }
        return psize;
    }


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {

        if (DEBUG) Logger.i(TAG, "onSurfaceCreated: \nGL10= " + gl.toString()
            + " \nEGLConfig= " + config.toString());

        mJustInit = true;

        synchronized (mTexturesBound) {
            // dispose old textures from memory that are not bound but haven't
            // been disposed yet
            final int mCleanupSize = mCleanup.size();
            for (int index = mCleanupSize - 1; index >= 0; index--) {
                disposeTexture(gl, mCleanup.get(index));
            }
            mCleanup.clear();

            // dispose old bound textures
            int mTexturesBoundSize = mTexturesBound.size();
            for (int i = mTexturesBoundSize - 1; i >= 0; i--) {
                disposeTexture(gl, mTexturesBound.get(i).mId);
                mTexturesBound.get(i).dispose();
            }
            mTexturesBound.clear();
        }

        mDefaultTexture = null;
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        if (DEBUG)
            Logger.i(TAG, "onSurfaceChanged: w - " + width + " h - " + height);

        mJustInit = true;
        mRenderable = true;

        final float aspectRatio = (float) width / (float) height;
        final float diffX = aspectRatio < 1.f ? 0.f : 1.f - aspectRatio;
        final float diffY = aspectRatio < 1.f ? 0.f : 1.f - aspectRatio;

        final float[] vertices = {
            -1.0f + diffX, -1.0f + diffY, 0.0f, // 0, Top Left
            -1.0f + diffX, 1.0f - diffY, 0.0f, // 1, Bottom Left
            1.0f - diffX, -1.0f + diffY, 0.0f, // 2, Bottom Right
            1.0f - diffX, 1.0f - diffY, 0.0f, // 3, Top Right
        };
        mMesh = new MeshSquare(vertices);

        // Set the background color to black ( rgba ).

        // Enable Smooth Shading, default not really needed.ы
        gl.glDisable(GL10.GL_LIGHTING);
        gl.glEnable(GL10.GL_BLEND);
        // Sets the current view port to the new size.
        gl.glViewport(0, 0, width, height);// OpenGL docs.
        // Select the projection matrix
        gl.glMatrixMode(GL10.GL_PROJECTION);// OpenGL docs.
        // Reset the projection matrix
        gl.glLoadIdentity();// OpenGL docs.
        // Calculate the aspect ratio of the window
        GLU.gluPerspective(gl, 45.0f, aspectRatio, 0.1f, 100.0f);
        // Select the modelview matrix
        gl.glMatrixMode(GL10.GL_MODELVIEW);// OpenGL docs.
        // Reset the modelview matrix
        gl.glLoadIdentity();// OpenGL docs.

        gl.glClearColor(27 / 255.0f, 31 / 255.0f, 36 / 255.0f, 1);

        // Enable textures
        gl.glEnable(GL10.GL_TEXTURE_2D);
        gl.glShadeModel(GL10.GL_SMOOTH);// OpenGL docs.
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);// OpenGL docs.

        getDefaultTexture(gl);
    }

    @NonNull
    private CoverTexture getDefaultTexture(GL10 gl) {
        if (mDefaultTexture == null) {
            mDefaultTexture = loadSingleTexture(gl, null, INVALID_BITMAP_IDENTIFIER, true);
        }
        return mDefaultTexture;
    }

    // FIXME: 11/4/16 need refactoring for onDrawFrame
    @Override
    public void onDrawFrame(GL10 gl) {
        //Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
        if (DEBUG)
            Logger.v(TAG, ">>> onDrawFrame start method @ " + this.toString());

        final int bitmapIdentifier = mBitmapIdentifier;

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT); // OpenGL docs.
        gl.glLoadIdentity();
        gl.glPushMatrix();
        gl.glTranslatef(0, 0, -2.4f);

        if (bitmapIdentifier != mOriginBitmapIdentifier.get()) {
            if (DEBUG) Logger.v(TAG, ">>> onDrawFrame ...");

            //If we show wrong index now, jump to corrent index, and adjust
            //shift as animation
            final int oldOriginTrackID = mOriginBitmapIdentifier.get();
            mOriginBitmapIdentifier.set(bitmapIdentifier);
            mShiftBitmapIdentifier.set(oldOriginTrackID);

            if (mJustInit) {
                mJustInit = false;
                mTouchShift = 0f;
            } else if (mAnimationState == ANIMATION_FROM_LEFT_TO_RIGHT) {
                mTouchShift = -1f;
                mAnimationState = ANIMATION_WITHOUT;
            } else if (mAnimationState == ANIMATION_FROM_RIGHT_TO_LEFT) {
                mTouchShift = 1f;
                mAnimationState = ANIMATION_WITHOUT;
            } else {
                mTouchShift = 0f;
            }

        } else if (mTouchShift != 0) {
            /*if (Math.abs(mTouchShift) == 1) {
            }*/

            mTouchShift *= 0.95f;
            if (Math.abs(mTouchShift) < 0.01f) {
                mTouchShift = 0.0f;
            }
        }

        // preload textures if possible
        CoverTexture textureOrigin = null;

        if (mOriginBitmapIdentifier.get() >= 0) {
            textureOrigin = getTextureByBitmapIdentifier(gl, mOriginBitmapIdentifier.get());
        }

        if (mShiftBitmapIdentifier.get() >= 0) {
            final CoverTexture textureShift = getTextureByBitmapIdentifier(gl, mShiftBitmapIdentifier.get());
            // there is available shift mesh ONLY if touch shift is no zero
            if (mTouchShift > 0) {
                mMesh.draw(gl, mTouchShift - 1, textureShift);
            } else if (mTouchShift < 0) {
                mMesh.draw(gl, mTouchShift + 1, textureShift);
            }
        }

        // draw origin mesh
        if (textureOrigin != null) {
            mMesh.draw(gl, mTouchShift, textureOrigin);
        }

        gl.glPopMatrix();

        // remove structures, free buffers
        synchronized (mTexturesBound) {
            int mTexturesBoundSize = mTexturesBound.size();
            int mCleanupSize = mCleanup.size();
            for (int i = mTexturesBoundSize - 1; i >= 0; i--) {
                CoverTexture texture = mTexturesBound.get(i);
                if (texture.mBitmapIdentifier != INVALID_BITMAP_IDENTIFIER
                    && texture.mBitmapIdentifier != mShiftBitmapIdentifier.get()
                    && texture.mBitmapIdentifier != mOriginBitmapIdentifier.get()) {
                    // remove this textre
                    texture.dispose();
                    mCleanup.add(texture.mId);
                    mTexturesBound.remove(i);
                }/* else if (texture.mCoverID != mOriginCoverID.get()) {
                    texture.mIsAutoCoverArt = false;
                }*/
            }
            // kill the textures
            for (int i = mCleanupSize - 1; i >= 0; i--) {
                disposeTexture(gl, mCleanup.get(i));
            }
            mCleanup.clear();
        }


        final float absTouchShift = Math.abs(mTouchShift);
        if (absTouchShift == 0 || absTouchShift == 1) {
            if (mCountCaverRendering >= NUMBER_OF_UPDATES_SURFACE) {
                mRenderable = false;
            }
            mCountCaverRendering++;
        }

        if (mRenderable) {
            requestRender();
        }
    }

    /**
     * Method loads up single texture
     *
     * @param gl           gl
     * @param bitmap       bitmap
     * @param recycleAfter recycleAfter
     */
    @NonNull
    private CoverTexture loadSingleTexture(GL10 gl,
                                           Bitmap bitmap,
                                           int trackID,
                                           boolean recycleAfter) {

        if (DEBUG) Logger.d(TAG, "loadSingleTexture()..");
        if (bitmap == null || bitmap.isRecycled()) {
            bitmap = getDefaultArtwork();
        }

        final CoverTexture texture = getTextureByBitmapIdentifier(trackID);
        if (texture.mTexture != null) {
            disposeTexture(gl, texture.mId);
            texture.dispose();
        }

        //First of all, resize bitmap to be pot texture
        try {
            final int width = bitmap.getWidth();
            final int height = bitmap.getHeight();
            final int potWid = nextPOT(width);
            final int potHei = nextPOT(height);
            final float u = (float) width / potWid;
            final float v = (float) height / potHei;

            final int[] textures = new int[1];
            gl.glGenTextures(1, textures, 0);

            if (DEBUG) {
                final String msg = "loadSingleTexture; bitmap: w - " + width
                    + " h - " + height + "; new bitmap: w - " + potWid
                    + " h - " + potHei + "; textures: " + Arrays.toString(textures)
                    + " u - " + u + " v - " + v;
                Logger.d(TAG, msg);
            }


            final Bitmap big = Bitmap.createBitmap(potWid, potHei, Bitmap.Config.ARGB_8888);
            if (!big.isPremultiplied()) {
                big.setPremultiplied(true);
            }
            mCanvas.setBitmap(big);
            mCanvas.drawBitmap(bitmap, 0, 0, null);


            gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
            gl.glTexParameterf(
                GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR
            );
            gl.glTexParameterf(
                GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST
            );
            GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, big, 0);
            gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
            gl.glFlush();
            texture.setTexture(textures[0], u, v);

            if (recycleAfter) {
                bitmap.recycle();
                bitmap = null;
            }
            big.recycle();

        } catch (Throwable error) { // Checking RunTime Exception?
            Logger.e(TAG, error.getMessage(), error);
            if (bitmap != null && recycleAfter) {
                bitmap.recycle();
            }
            //texture.mPreload = true;
            return getDefaultTexture(gl);
        }

        if (DEBUG) Logger.d(TAG, "loadSingleTexture()..ended");

        return texture;
    }


    @NonNull
    private CoverTexture getTextureByBitmapIdentifier(int bitmapIdentifier) {
        CoverTexture texture = null;

        synchronized (mTexturesBound) {
            int mTexturesBoundSize = mTexturesBound.size();
            for (int i = mTexturesBoundSize - 1; i >= 0; i--) {
                if (mTexturesBound.get(i).mBitmapIdentifier == bitmapIdentifier)
                    texture = mTexturesBound.get(i);
            }
        }

        if (texture == null) {
            texture = new CoverTexture(bitmapIdentifier);
            synchronized (mTexturesBound) {
                mTexturesBound.add(texture);
            }
        }

        return texture;
    }


    @NonNull
    private CoverTexture getTextureByBitmapIdentifier(GL10 gl, int bitmapIdentifier) {
        final CoverTexture texture = getTextureByBitmapIdentifier(bitmapIdentifier);

        if (texture.mTexture == null) {
            Bitmap bm = null;
            if (mTouchShift == 0.0f || Math.abs(mTouchShift) == 1.0f) {
                if (mBitmap != null && mBitmapIdentifier == bitmapIdentifier) {
                    bm = mBitmap;
                }
                if (   bm == null
                    || bm.isRecycled()
                    || texture.mBitmapIdentifier != bitmapIdentifier) {
                    bm = null;
                }
            }
            if (bm == null) {
                return getDefaultTexture(gl);
            } else {
                return loadSingleTexture(gl, bm, bitmapIdentifier, false);
            }
        }

        return texture;
    }

    private void reloadCover() {
        disposeTextures();
        setRenderable(true);
    }

    /**
     * This method disposes all currently bound textures
     */
    public void disposeTextures() {
        synchronized (mTexturesBound) {
            for (int i = 0; i < mTexturesBound.size(); i++) {
                CoverTexture texture = mTexturesBound.get(i);
                if (texture.mBitmapIdentifier == INVALID_BITMAP_IDENTIFIER) {
                    continue;
                }
                mCleanup.add(mTexturesBound.get(i).mId);
                mTexturesBound.get(i).dispose();
            }
            mTexturesBound.clear();
        }
    }

    /**
     * Remove gl texture from memory
     *
     * @param gl        gl
     * @param textureId textureId
     */
    private static void disposeTexture(GL10 gl, int textureId) {
        if (textureId > -1) {
            CLEANUP_TEXTURE_INDICES[0] = textureId;
            gl.glDeleteTextures(
                CLEANUP_TEXTURE_INDICES.length, CLEANUP_TEXTURE_INDICES, 0
            );
            gl.glFlush();
        }
    }


    public boolean isDraw() {
        return mRenderable;
    }


    @NonNull
    private Bitmap getDefaultArtwork() {
        final Bitmap bitmap = Utils.getBitmapFromResources(
            getContext(), R.drawable.albumart_mp_unknown
        );
        if (DEBUG) Logger.d(TAG, "Default artWork: w - " + bitmap.getWidth()
            + " h - " + bitmap.getHeight());

        return bitmap;
    }


    @Override
    public String toString() {
        return "CustomGLSurfaceView{" +
            " countRendering=" + mCountCaverRendering +
            ", mOriginBitmapIdentifier=" + mOriginBitmapIdentifier.get() +
            ", mShiftBitmapIdentifier=" + mShiftBitmapIdentifier.get() +
            ", mTouchShift=" + mTouchShift +
            ", mJustInit=" + mJustInit +
            ", mCleanup=" + mCleanup +
            //", trackID=" + (mTrack != null ? mTrack.getID() : -1) +
            //", trackTitle=" + (mTrack != null ? mTrack.getTitle() : null) +
            '}';
    }

    /*public void setProgressIndicatorListener(IProgressIndicator listener) {
        mProgressIndicatorListener = listener;
    }*/

    private static final class CoverTexture {

        private float[] mTexture;
        //public float mU;
        //public float mV;
        int mId;
        private long mBitmapIdentifier;
        FloatBuffer mBuffer;
        //private boolean mPreload = false;
        //private boolean mIsAutoCoverArt = false;

        private CoverTexture(int bitmapIdentifier) {
            mBitmapIdentifier = bitmapIdentifier;
            mId = -1;
        }

        private void setTexture(int id, float u, float v) {
            mId = id;

            mTexture = new float[]{
                0, v,
                0, 0,
                u, v,
                u, 0
            };

            ByteBuffer bb = ByteBuffer.allocateDirect(mTexture.length * 4);
            bb.order(ByteOrder.nativeOrder());
            mBuffer = bb.asFloatBuffer();
            mBuffer.put(mTexture);
            mBuffer.position(0);
        }

        private void dispose() {
            //mPreload = false;
            mTexture = null;
            if (mBuffer != null) {
                mBuffer.clear();
            }
        }
    }


    private static final class MeshSquare {

        //public static final int ALIGN_LEFT = 0;
        //public static final int ALIGN_RIGHT = 1;

        private FloatBuffer mVertexBuffer;
        private ShortBuffer mIndexBuffer;

        private static final short[] INDICIES = new short[]{0, 1, 2, 2, 1, 3};

        private MeshSquare(float[] vertices) {
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
            byteBuffer.order(ByteOrder.nativeOrder());
            byteBuffer.position(0);
            mVertexBuffer = byteBuffer.asFloatBuffer();
            mVertexBuffer.put(vertices);
            mVertexBuffer.position(0);

            ByteBuffer ibb = ByteBuffer.allocateDirect(INDICIES.length * 2);
            ibb.order(ByteOrder.nativeOrder());
            mIndexBuffer = ibb.asShortBuffer();
            mIndexBuffer.put(INDICIES);
            mIndexBuffer.position(0);
        }

        private void draw(GL10 gl, float balance, CoverTexture texture) {
            //ok, let check matrix!
            gl.glPushMatrix();

            gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

            // Telling OpenGL to enable textures.
            gl.glEnable(GL10.GL_TEXTURE_2D);

            /*
             * DEPEND ON ALIGN, how to draw it
             */
            gl.glRotatef(45.0f * balance, 0.0f, 1.0f, 0.0f);
            gl.glTranslatef(0.33f * 2 * balance, 0.0f, 2 * Math.abs(balance));
            gl.glColor4f(1.0f, 1.0f, 1.0f, (1 - Math.abs(balance)));

            //bind texture with specific id
            gl.glBindTexture(GL10.GL_TEXTURE_2D, texture.mId);
            //bind buffer
            gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texture.mBuffer);
            //bind vertices
            gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);

            gl.glDrawElements(GL10.GL_TRIANGLES, INDICIES.length, GL10.GL_UNSIGNED_SHORT, mIndexBuffer);

            gl.glDisable(GL10.GL_TEXTURE_2D);

            gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
            gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

            gl.glPopMatrix();
        }
    }


}
