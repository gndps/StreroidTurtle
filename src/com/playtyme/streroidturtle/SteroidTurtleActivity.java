package com.playtyme.streroidturtle;

//import matim.development.KeyEvent;
//import matim.development.Override;
//import matim.development.SplashTemplate.SceneType;

//import org.andengine.engine.Engine;
import org.andengine.engine.options.EngineOptions;
import org.andengine.entity.scene.Scene;
//import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.util.GLState;
//import org.andengine.ui.IGameInterface.OnCreateResourcesCallback;
//import org.andengine.ui.IGameInterface.OnCreateSceneCallback;
//import org.andengine.ui.IGameInterface.OnPopulateSceneCallback;
import org.andengine.ui.activity.BaseGameActivity;
//import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.input.touch.TouchEvent;

import android.view.KeyEvent;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.debug.Debug;
 
import java.io.IOException;
import java.io.InputStream;

public class SteroidTurtleActivity extends BaseGameActivity
{
	private static int CAMERA_WIDTH = 480;
	private static int CAMERA_HEIGHT = 320;
	//private Camera camera;
	private Scene splashScene;
	private Scene mainScene;
	private Scene optionsScene;
	
    private BitmapTextureAtlas splashTextureAtlas;
    private ITextureRegion splashTextureRegion;
    private Sprite splash;
    private enum SceneType
	{
		SPLASH,
		MAIN,
		OPTIONS	
	}
    private SceneType currentScene = SceneType.SPLASH;
    private ITextureRegion mgameBackground, moptionsMenu, mplayButton, maboutButton;

	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, 
		    new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
		// TODO Auto-generated method stub
	}

	@Override
	public void onCreateResources(OnCreateResourcesCallback pOnCreateResourcesCallback) throws Exception
	{
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
        splashTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 480, 320, TextureOptions.DEFAULT);
        splashTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(splashTextureAtlas, this, "splash.png", 0, 0);
        splashTextureAtlas.load();
       
        pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback) throws Exception
	{
		initSplashScene();
        pOnCreateSceneCallback.onCreateSceneFinished(this.splashScene);
	}

	@Override
	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception
	{
		mEngine.registerUpdateHandler(new TimerHandler(1f, new ITimerCallback() 
		{
            public void onTimePassed(final TimerHandler pTimerHandler) 
            {
                mEngine.unregisterUpdateHandler(pTimerHandler);
                loadResources();
                loadScenes();         
                splash.detachSelf();
                mEngine.setScene(optionsScene);
                currentScene = SceneType.OPTIONS;
            }
		}));
  
		pOnPopulateSceneCallback.onPopulateSceneFinished();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) 
	{  
	    if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN)
	    {	    	
	    	switch (currentScene)
	    	{
	    		case SPLASH:
	    			break;
	    		case OPTIONS:
	    			System.exit(0);
	    			break;
	    		case MAIN:
	    			mEngine.setScene(optionsScene);
	    			currentScene = SceneType.OPTIONS;
	    			break;
	    	}
	    }
	    return false; 
	}
	
	public void loadResources() 
	{
		// Load your game resources here!
		try {
		    // 1 - Set up bitmap textures
		    ITexture gameBackground = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
		        @Override
		        public InputStream open() throws IOException {
		            return getAssets().open("gfx/background.png");
		        }
		    });
		    ITexture optionsMenu = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
		        @Override
		        public InputStream open() throws IOException {
		            return getAssets().open("gfx/samplemain.png");
		        }
		    });
		    ITexture playButton = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
		        @Override
		        public InputStream open() throws IOException {
		            return getAssets().open("gfx/play_button.png");
		        }
		    });
		    ITexture aboutButton = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
		        @Override
		        public InputStream open() throws IOException {
		            return getAssets().open("gfx/about_button.png");
		        }
		    });
		    // 2 - Load bitmap textures into VRAM
		    gameBackground.load();
		    optionsMenu.load();
		    playButton.load();
		    aboutButton.load();
		    this.mgameBackground = TextureRegionFactory.extractFromTexture(gameBackground);
		    this.moptionsMenu = TextureRegionFactory.extractFromTexture(optionsMenu);
		    this.maboutButton = TextureRegionFactory.extractFromTexture(aboutButton);
		    this.mplayButton = TextureRegionFactory.extractFromTexture(playButton);
		} catch (IOException e) {
		    Debug.e(e);
		}
		
	}
	
	private void loadScenes()
	{
		// load your game here, you scenes
		optionsScene = new Scene();
		Sprite optionsSprite = new Sprite(0, 0, this.moptionsMenu, getVertexBufferObjectManager());
		Sprite playButtonSprite = new Sprite(340, 120, this.mplayButton, getVertexBufferObjectManager()){
			
		      
			@Override
		      public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) 
		      {
		    	  mEngine.setScene(mainScene);
		    	  currentScene=SceneType.MAIN;
		     //Insert Code Here
	         return true;
		      }
		};

	
	//===============================================
	//	ADD ON TOUCH EVENT FOR PLAY AND ABOUT BUTTON 
	//===============================================
		
		Sprite aboutButtonSprite = new Sprite(340, 180, this.maboutButton, getVertexBufferObjectManager());
		Sprite gameBackgroundSprite = new Sprite(0, 0, this.mgameBackground, getVertexBufferObjectManager());
		
		optionsScene.attachChild(optionsSprite);
		optionsScene.attachChild(playButtonSprite);
		optionsScene.attachChild(aboutButtonSprite);
		mainScene = new Scene();
		mainScene.attachChild(gameBackgroundSprite);
		optionsScene.registerTouchArea(playButtonSprite);
		optionsScene.registerTouchArea(aboutButtonSprite);
		
	}
	
	// ===========================================================
	// INITIALIZIE  
	// ===========================================================
	
	private void initSplashScene()
	{
    	splashScene = new Scene();
    	splash = new Sprite(0, 0, splashTextureRegion, mEngine.getVertexBufferObjectManager())
    	{
    		@Override
            protected void preDraw(GLState pGLState, Camera pCamera) 
    		{
                super.preDraw(pGLState, pCamera);
                pGLState.enableDither();
            }
    	};
    	
    	splash.setScale(1.0f);
    	splash.setPosition((CAMERA_WIDTH - splash.getWidth()) * 0.5f, (CAMERA_HEIGHT - splash.getHeight()) * 0.5f);
    	splashScene.attachChild(splash);
	}
}
