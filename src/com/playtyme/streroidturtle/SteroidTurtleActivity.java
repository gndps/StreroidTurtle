package com.playtyme.streroidturtle;

//import matim.development.KeyEvent;
//import matim.development.Override;
//import matim.development.SplashTemplate.SceneType;

//import org.andengine.engine.Engine;
import java.io.IOException;
import java.io.InputStream;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.timer.ITimerCallback;
import org.andengine.engine.handler.timer.TimerHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.util.GLState;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.debug.Debug;

import android.hardware.SensorManager;
import android.view.KeyEvent;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class SteroidTurtleActivity extends BaseGameActivity
{
	private static int CAMERA_WIDTH = 480;
	private static int CAMERA_HEIGHT = 320;
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
		OPTIONS,
	}
    private SceneType currentScene = SceneType.SPLASH;
    private ITextureRegion moptionsMenu, mplayButton, maboutButton;
    private BitmapTextureAtlas mBitmapTextureAtlas;
	private TiledTextureRegion mPlayerTextureRegion;
	//private TiledTextureRegion mEnemyTextureRegion;
	private BitmapTextureAtlas mAutoParallaxBackgroundTexture;
	private ITextureRegion mParallaxLayerBack;
	private ITextureRegion mParallaxLayerMid;
	private ITextureRegion mParallaxLayerFront;
	private ITextureRegion jumpButtonImage;
	private ITextureRegion rollButtonImage;
	private ITextureRegion consumeButtonImage;
	private ITextureRegion antidoteButtonImage;
	private PhysicsWorld mPhysicsWorld;
	private static final FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);

	//Method for generating Toast messages as they need to run on UI thread
	
	public void gameToast(final String msg) {
	    this.runOnUiThread(new Runnable() {
	        @Override
	        public void run() {
	        	Toast.makeText(SteroidTurtleActivity.this, msg, Toast.LENGTH_SHORT).show();
	        }
	    });
	}

	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, 
		    new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), camera);
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
	    	//		gameHUD.detachSelf();//------------------>>>HUD DETACHED
	    			break;
	    	}
	    }
	    return false; 
	}
	
	public void loadResources() 
	{
		// TODO Load resources
		try {
		    // 1 - Set up bitmap textures
		    
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
		    ITexture jumpButton = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
		        @Override
		        public InputStream open() throws IOException {
		            return getAssets().open("gfx/jump.png");
		        }
		    });
		    ITexture rollButton = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
		        @Override
		        public InputStream open() throws IOException {
		            return getAssets().open("gfx/roll.png");
		        }
		    });
		    ITexture consumeButton = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
		        @Override
		        public InputStream open() throws IOException {
		            return getAssets().open("gfx/consume.png");
		        }
		    });
		    ITexture antidoteButton = new BitmapTexture(this.getTextureManager(), new IInputStreamOpener() {
		        @Override
		        public InputStream open() throws IOException {
		            return getAssets().open("gfx/antidote.png");
		        }
		    });
		    // 2 - Load bitmap textures into VRAM
		//    gameBackground.load();
		    optionsMenu.load();
		    playButton.load();
		    aboutButton.load();
		    jumpButton.load();
		    rollButton.load();
		    antidoteButton.load();
		    consumeButton.load();
		  //  this.mgameBackground = TextureRegionFactory.extractFromTexture(gameBackground);
		    this.moptionsMenu = TextureRegionFactory.extractFromTexture(optionsMenu);
		    this.maboutButton = TextureRegionFactory.extractFromTexture(aboutButton);
		    this.mplayButton = TextureRegionFactory.extractFromTexture(playButton);
		    this.rollButtonImage = TextureRegionFactory.extractFromTexture(rollButton);
		    this.jumpButtonImage = TextureRegionFactory.extractFromTexture(jumpButton);
		    this.consumeButtonImage = TextureRegionFactory.extractFromTexture(consumeButton);
		    this.antidoteButtonImage = TextureRegionFactory.extractFromTexture(antidoteButton);
		    BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
			
			this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(), 256, 128, TextureOptions.BILINEAR);
			this.mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "player.png", 0, 0, 3, 4);
		//	this.mEnemyTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas, this, "enemy.png", 73, 0, 3, 4);
			this.mBitmapTextureAtlas.load();

			this.mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(this.getTextureManager(), 1024, 1024);
			this.mParallaxLayerFront = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_front.png", 0, 0);
			this.mParallaxLayerBack = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_back.png", 0, 188);
			this.mParallaxLayerMid = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_mid.png", 0, 669);
			this.mAutoParallaxBackgroundTexture.load();
		} catch (IOException e) {
		    Debug.e(e);
		}
		
	}
	
	private void loadScenes()
	{
		//PHYSICS WORLD
		VertexBufferObjectManager vertexBufferObjectManager= this.getVertexBufferObjectManager();
		try {

			this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
			//final Vector2 gravity = Vector2Pool.obtain(0,10);
			//this.mPhysicsWorld.setGravity(gravity);
			} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			//gameToast("Exception thrown in load scenes");
			
		}
		
		// TODO Load Scenes
		optionsScene = new Scene();
		Sprite optionsSprite = new Sprite(0, 0, this.moptionsMenu, getVertexBufferObjectManager());
		Sprite playButtonSprite = new Sprite(340, 120, this.mplayButton, getVertexBufferObjectManager()){
			
		      
			@Override
		      public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) 
		      {
		    	  mEngine.setScene(mainScene);
		    	  currentScene=SceneType.MAIN;
		    //	  camera.setHUD(gameHUD);//-------------------->>>>> set HUD
		    	
		     //Insert Code Here
	         return true;
		      }
		};
		Sprite jumpButtonSprite = new Sprite(360,235, this.jumpButtonImage, getVertexBufferObjectManager()){
			
		      
			@Override
		      public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) 
		      {
		    	  //ADD JUMP FUNCTIONALITY !!!!!!!!!!
				
		    
	         return true;
		      }
		};
		Sprite rollButtonSprite = new Sprite(0,235, this.rollButtonImage, getVertexBufferObjectManager()){
			
		      
			@Override
		      public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) 
		      {
		    	  //ADD ROLL FUNCTIONALITY !!!!!!!!!!
		    
	         return true;
		      }
		};
		Sprite consumeButtonSprite = new Sprite(120,235, this.consumeButtonImage, getVertexBufferObjectManager()){
			
		      
			@Override
		      public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) 
		      {
		    	  //ADD CONSUME FUNCTIONALITY !!!!!!!!!!
		    
	         return true;
		      }
		};
		Sprite antidoteButtonSprite = new Sprite(240,235, this.antidoteButtonImage, getVertexBufferObjectManager()){
			
		      
			@Override
		      public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) 
		      {
		    	  //ADD antidote FUNCTIONALITY !!!!!!!!!!
		    
	         return true;
		      }
		};
		

	
	//===============================================
	//	ADD ON TOUCH EVENT FOR PLAY AND ABOUT BUTTON 
	//===============================================
	
		Sprite aboutButtonSprite = new Sprite(340, 180, this.maboutButton, getVertexBufferObjectManager()){
			
		      
			@Override
		      public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) 
		      {
				String msg= "Developed By PlayTyme™";
				gameToast(msg);
	         return true;
		      }
		};
	//	Sprite gameBackgroundSprite = new Sprite(0, 0, this.mgameBackground, getVertexBufferObjectManager());
		
		optionsScene.attachChild(optionsSprite);
		optionsScene.attachChild(playButtonSprite);
		optionsScene.attachChild(aboutButtonSprite);
		mainScene = new Scene();
		/////////////////////////////////////////////
		//adding auto parallax here
		/////////////////////////////////////////////
		this.mEngine.registerUpdateHandler(new FPSLogger());
	//	final Scene scene = new Scene();
		final AutoParallaxBackground autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		//final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager();
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerBack.getHeight(), this.mParallaxLayerBack, vertexBufferObjectManager)));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, new Sprite(0, 80, this.mParallaxLayerMid, vertexBufferObjectManager)));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(), this.mParallaxLayerFront, vertexBufferObjectManager)));
		

		/* Calculate the coordinates for the player, so its centered on the camera. */
		final float playerX = CAMERA_WIDTH/5;
		final float playerY = 0;//CAMERA_HEIGHT - this.mPlayerTextureRegion.getHeight() - 90;

		/* Create two sprites and add it to the scene. */
		final AnimatedSprite player = new AnimatedSprite(playerX, playerY, this.mPlayerTextureRegion, vertexBufferObjectManager);
		final Body playerBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, player, BodyType.DynamicBody, FIXTURE_DEF);
		//player.setScaleCenterY(this.mPlayerTextureRegion.getHeight());
		//player.setScale(2);
		player.animate(new long[]{100, 100, 100}, 3, 5, true);
		
	//	final AnimatedSprite enemy = new AnimatedSprite(playerX - 80, playerY, this.mEnemyTextureRegion, vertexBufferObjectManager);
	//	enemy.setScaleCenterY(this.mEnemyTextureRegion.getHeight());
	//	enemy.setScale(2);
	//	enemy.animate(new long[]{200, 200, 200}, 3, 5, true);

		
		
		/////parallax end/////
		mainScene.attachChild(rollButtonSprite);
		mainScene.attachChild(jumpButtonSprite);
		mainScene.attachChild(consumeButtonSprite);
		mainScene.attachChild(antidoteButtonSprite);
		
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 85, CAMERA_WIDTH, 85, vertexBufferObjectManager);
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		ground.setAlpha(new Float(0.0));
		
		this.mainScene.attachChild(ground);
		this.mainScene.registerUpdateHandler(this.mPhysicsWorld);
	
		
		//mainScene.attachChild(gameBackgroundSprite);
		
		mainScene.setBackground(autoParallaxBackground);
		mainScene.attachChild(player);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(player, playerBody, true, true));
		
		
	//	mainScene.attachChild(enemy);
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

