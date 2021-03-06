package GraviTux;

import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL;
import org.newdawn.slick.*;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;
import org.newdawn.slick.tiled.TiledMap;

class Play extends BasicGameState
{
    ////deceleration of global variables

    //Animations for tux movement
    private Animation tux, bottomStanding, bottomMovingLeft, bottomMovingRight, topStanding, topMovingLeft,
            topMovingRight, leftStanding, leftMovingUp, leftMovingDown, rightStanding, rightMovingUp, rightMovingDown, snowStorm;
    private final TiledMap[] worldMap;  //Level in the background
    private boolean[][] blocked, deadly, levelEnd, storm;   //2 dimensional arrays for collision detection
    private char gravity;       //indicates direction of gravity
    private int tuxWidth, tuxHeight, levelCurrent;  //tux image size and number of current level
    private Timer inputDelay, levelTime, gravityTimer;        //timer to prevent things from going too fast
    private float tuxX, tuxY, gravitySpeed;     //tux position and falling speed
    private static final int duration = 300;    //length of the walk animation
    private static final int size = 40;         //tiled size in px
    private static final int levelMax = 16;       //max level
    private static final float moveSpeed = 0.25f;   //tux movement speed
    private static final float gravityAcc = 0.02f;  //tux acceleration speed when falling
    private static final float gravitySpeedMax = 7f;  //tux maximum falling speed
    //ingame images
    private Image bg, mbutton, level, number0, number1, number2, number3, number4, number5, number6, number7, number8, number9;
    //ingame sounds
    private Sound storms, win, gravitation, bmusic, run, start, die, hole;

    ////constructor
    public Play()
    {
        levelCurrent = 0;   //current level (starting at 0)
        worldMap = new TiledMap[levelMax];  //array for levels
    }

    ////INIT METHOD
    @Override
    public void init(GameContainer gc, StateBasedGame sbg) throws SlickException
    {
        //lvl background
        bg = new Image("res/GraviTux/level/BG_v4.png");

        //menu
        mbutton = new Image("res/GraviTux/level/menu.png");
        level = new Image("res/GraviTux/level/level.png");
        number0 = new Image("res/GraviTux/level/numbers/number0.png");
        number1 = new Image("res/GraviTux/level/numbers/number1.png");
        number2 = new Image("res/GraviTux/level/numbers/number2.png");
        number3 = new Image("res/GraviTux/level/numbers/number3.png");
        number4 = new Image("res/GraviTux/level/numbers/number4.png");
        number5 = new Image("res/GraviTux/level/numbers/number5.png");
        number6 = new Image("res/GraviTux/level/numbers/number6.png");
        number7 = new Image("res/GraviTux/level/numbers/number7.png");
        number8 = new Image("res/GraviTux/level/numbers/number8.png");
        number9 = new Image("res/GraviTux/level/numbers/number9.png");

        //lvl counter
        for (int i = 0; i < levelMax; i++)  //loads levels
        {
            worldMap[i] = new TiledMap("res/GraviTux/level/level_" + (i + 1) + ".tmx");
        }

        //sounds, for some reason ogg do not work correctly....
        storms = new Sound("res/GraviTux/sounds/eissturm.wav");
        win = new Sound("res/GraviTux/sounds/gewonnen.wav");
        gravitation = new Sound("res/GraviTux/sounds/gravitation.wav");
        bmusic = new Sound("res/GraviTux/sounds/hintergrund.wav");
        run = new Sound("res/GraviTux/sounds/laufen.wav");
        start = new Sound("res/GraviTux/sounds/start.wav");
        die = new Sound("res/GraviTux/sounds/sterben.wav");
        hole = new Sound("res/GraviTux/sounds/wasserloch.wav");

        ////Filling Image arrays for standing animation
        Image[] bottomStand = {new Image("GraviTux/tux/Tux_stand.png")};   //tux standing on ground
        Image[] topStand = new Image[1];
        topStand[0] = bottomStand[0].getFlippedCopy(false, true);   //tux standing upside down
        Image[] leftStand = {new Image("GraviTux/tux/Tux_stand.png")};     //tux standing left
        leftStand[0].rotate(90f);
        Image[] rightStand = {new Image("GraviTux/tux/Tux_stand.png")};    //tux standing right
        rightStand[0].rotate(-90f);

        Image[] bottomWalkLeft = new Image[8];  //initializing image arrays
        Image[] bottomWalkRight = new Image[8];
        Image[] topWalkLeft = new Image[8];
        Image[] topWalkRight = new Image[8];
        Image[] leftWalkUp = new Image[8];
        Image[] leftWalkDown = new Image[8];
        Image[] rightWalkUp = new Image[8];
        Image[] rightWalkDown = new Image[8];

        for (int i = 0; i < 8; i++) //filling image arrays for moving animation
        {
            bottomWalkLeft[i] = new Image("GraviTux/tux/Tux_0" + (i + 1) + ".png"); //walk left
            bottomWalkRight[i] = bottomWalkLeft[i].getFlippedCopy(true, false);     //walk right
            topWalkLeft[i] = bottomWalkLeft[i].getFlippedCopy(false, true);         //walk left upside down
            topWalkRight[i] = bottomWalkRight[i].getFlippedCopy(false, true);       //walk right upside down
            leftWalkUp[i] = new Image("GraviTux/tux/Tux_0" + (i + 1) + ".png");     //walk up on the left side
            leftWalkUp[i].rotate(90f);
            leftWalkDown[i] = new Image("GraviTux/tux/Tux_0" + (i + 1) + ".png").getFlippedCopy(true, false);   //walk down on the left side
            leftWalkDown[i].rotate(90f);
            rightWalkUp[i] = new Image("GraviTux/tux/Tux_0" + (i + 1) + ".png").getFlippedCopy(true, false);    //walk up on the right side
            rightWalkUp[i].rotate(-90f);
            rightWalkDown[i] = new Image("GraviTux/tux/Tux_0" + (i + 1) + ".png");   //walk down on the right side
            rightWalkDown[i].rotate(-90f);
        }

        Image[] snowImages = new Image[]{new Image("GraviTux/snowstorm/eissturm_04.png"), new Image("GraviTux/snowstorm/eissturm_03.png"),
                new Image("GraviTux/snowstorm/eissturm_02.png"), new Image("GraviTux/snowstorm/eissturm_01.png")};
        snowStorm = new Animation(snowImages, 50, true);

        //filling animation variables with the image arrays
        bottomStanding = new Animation(bottomStand, duration, false);
        topStanding = new Animation(topStand, duration, false);
        leftStanding = new Animation(leftStand, duration, false);
        rightStanding = new Animation(rightStand, duration, false);
        bottomMovingLeft = new Animation(bottomWalkLeft, duration, false);
        bottomMovingRight = new Animation(bottomWalkRight, duration, false);
        topMovingLeft = new Animation(topWalkLeft, duration, false);
        topMovingRight = new Animation(topWalkRight, duration, false);
        leftMovingUp = new Animation(leftWalkUp, duration, false);
        leftMovingDown = new Animation(leftWalkDown, duration, false);
        rightMovingUp = new Animation(rightWalkUp, duration, false);
        rightMovingDown = new Animation(rightWalkDown, duration, false);

        //build collision maps based on tile properties in the Tiled map
        blocked = new boolean[worldMap[levelCurrent].getWidth()][worldMap[levelCurrent].getHeight()];
        deadly = new boolean[worldMap[levelCurrent].getWidth()][worldMap[levelCurrent].getHeight()];
        levelEnd = new boolean[worldMap[levelCurrent].getWidth()][worldMap[levelCurrent].getHeight()];
        storm = new boolean[worldMap[levelCurrent].getWidth()][worldMap[levelCurrent].getHeight()];

        for (int xAxis = 0; xAxis < 20; xAxis++)
        {
            for (int yAxis = 0; yAxis < 15; yAxis++)
            {
                int tileID = worldMap[levelCurrent].getTileId(xAxis, yAxis, 0);

                if ("true".equals(worldMap[levelCurrent].getTileProperty(tileID, "blocked", "false")))
                {
                    blocked[xAxis][yAxis] = true;   //wall contact array
                }
                if ("true".equals(worldMap[levelCurrent].getTileProperty(tileID, "die", "false")))
                {
                    deadly[xAxis][yAxis] = true;    //deadly contact array
                }
                if ("true".equals(worldMap[levelCurrent].getTileProperty(tileID, "fish", "false")))
                {
                    levelEnd[xAxis][yAxis] = true;  //level finished array
                }
                if ("true".equals(worldMap[levelCurrent].getTileProperty(tileID, "rotate", "false")))
                {
                    storm[xAxis][yAxis] = true; //storm to rotate gravity
                    //worldMap[levelCurrent].
                }
            }
        }

        //sets gravity, animation and tux position to default
        tuxWidth = 23;       //tux is illuminati wide
        tuxHeight = 42;       //tux size has the answer to the world, the universe and all the rest.
        tuxX = 79;     //tux start coordinates (79 = start)
        tuxY = 518;    //518 is default
        tux = bottomStanding; //tux looks towards the player, when the game starts
        gravity = 'b';  //default gravity direktion
        gravitySpeed = 0f;  //tux current falling speed

        inputDelay = new Timer(300);    //timer to prevent some things from happening too fast
        levelTime = new Timer(1);    //timer to see how long you took for the level
        gravityTimer = new Timer(300); //for proper gravity rotation
    }

    ////RENDER METHOD
    @Override
    public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException
    {
        bg.draw(0, 0);  //draw background
        worldMap[levelCurrent].render(0, 0); //draw the map at 0,0
        //mborder.draw(0, 0); //draw menu
        mbutton.draw(710, 5);
        level.draw(5, 5);
        switch (levelCurrent)    //current level indicator
        {
            case 0:
                number1.draw(70, 4);
                break;
            case 1:
                number2.draw(70, 4);
                break;
            case 2:
                number3.draw(70, 4);
                break;
            case 3:
                number4.draw(70, 4);
                break;
            case 4:
                number5.draw(70, 4);
                break;
            case 5:
                number6.draw(70, 4);
                break;
            case 6:
                number7.draw(70, 4);
                break;
            case 7:
                number8.draw(70, 4);
                break;
            case 8:
                number9.draw(70, 4);
                break;
            case 9:
                number1.draw(70, 4);
                number0.draw(78, 4);
                break;
            case 10:
                number1.draw(70, 4);
                number1.draw(78, 4);
                break;
            case 11:
                number1.draw(70, 4);
                number2.draw(78, 4);
                break;
            case 12:
                number1.draw(70, 4);
                number3.draw(78, 4);
                break;
            case 13:
                number1.draw(70, 4);
                number4.draw(78, 4);
                break;
            case 14:
                number1.draw(70, 4);
                number5.draw(78, 4);
                break;
            case 15:
                number1.draw(70, 4);
                number6.draw(78, 4);
                break;
            case 16:
                number1.draw(70, 4);
                number7.draw(78, 4);
                break;
            //42
            default:
                number1.draw(70, 4);
                break;
        }
        for (int xAxis = 0; xAxis < 20; xAxis++)    //draws animated snowstorms
        {
            for (int yAxis = 0; yAxis < 15; yAxis++)
            {
                if (storm[xAxis][yAxis])
                {
                    snowStorm.draw(xAxis * size, yAxis * size);
                }
            }
        }

        tux.draw((int) tuxX, (int) tuxY);   //draws tux at 79, 518 (bottom left)
        //g.drawString("Tux X: " + (int) tuxX + "\nTux Y: " + (int) tuxY, 650, 50);   //tux position indicator

        //g.drawString("Time: " + levelTime.getTime(), 360, 10);  //game timer   //zuviel grafikaufwand....

        /*
		if (menu)   //when the player presses escape
		{
			g.drawString("Weiter spielen (S)", 324, 200);
			g.drawString("Hauptmenü (M)", 324, 250);
			g.drawString("Spiel beenden (Q)", 324, 300);
		}*/
    }

    ////UPDATE METHOD
    @Override
    public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException
    {
        int posX = Mouse.getX();
        int posY = Mouse.getY();
        Input input = gc.getInput();    //gets keyboard input
        inputDelay.addTime(delta);   //updates timer
        levelTime.addTime(delta);
        gravityTimer.addTime(delta);

        float movingSpeed = moveSpeed * delta;    //makes movement speed dependent on refresh rate
        float fallingSpeed = gravitySpeed + gravityAcc * delta; //same for falling, but with acceleration

        ////move left, when gravity is bottom or top
        if ((input.isKeyDown(Input.KEY_LEFT) || input.isKeyDown(Input.KEY_A)) && (gravity == 'b' || gravity == 't'))
        {
            setAnimation(input);        //changes the animation, so tux faces left
            moveTux(-movingSpeed, 0, delta);   //moves tux
        }
        ////move right, when gravity is bottom or top
        if ((input.isKeyDown(Input.KEY_RIGHT) || input.isKeyDown(Input.KEY_D)) && (gravity == 'b' || gravity == 't'))
        {
            setAnimation(input);
            moveTux(movingSpeed, 0, delta);
        }
        ////move up, when gravity is rotated by +/- 90%
        if ((input.isKeyDown(Input.KEY_UP) || input.isKeyDown(Input.KEY_W)) && (gravity == 'l' || gravity == 'r'))
        {
            setAnimation(input);
            moveTux(0, -movingSpeed, delta);
        }
        ////move down, when gravity is rotated by +/- 90%
        if ((input.isKeyDown(Input.KEY_DOWN) || input.isKeyDown(Input.KEY_S)) && (gravity == 'l' || gravity == 'r'))
        {
            setAnimation(input);
            moveTux(0, movingSpeed, delta);
        }

        ////flip gravity, but only when touching the ground
        if ((input.isKeyDown(Input.KEY_SPACE) || input.isKeyDown(Input.KEY_X)) && gravitySpeed == 0 && inputDelay.isTimeElapsed())
        {
            flipGravity();  ///reverse gravity
            gravitation.play();
        }

        ////Gravity bottom
        if (gravity == 'b' && !(collision(tuxX + 2, tuxY + tuxHeight + fallingSpeed - 1, blocked)
                || collision(tuxX + tuxWidth - 2, tuxY + tuxHeight + fallingSpeed - 1, blocked)))
        {
            fall(delta);
        }
        ////gravity top
        else if (gravity == 't' && !(collision(tuxX + 2, tuxY - fallingSpeed, blocked)
                || collision(tuxX + tuxWidth - 2, tuxY - fallingSpeed, blocked)))
        {
            fall(delta);
        }
        ////gravity left
        else if (gravity == 'l' && !(collision(tuxX - fallingSpeed, tuxY + 2, blocked)
                || collision(tuxX - fallingSpeed, tuxY + tuxHeight - 2, blocked)))
        {
            fall(delta);
        }
        //gravity right
        else if (gravity == 'r' && !(collision(tuxX + tuxWidth + fallingSpeed, tuxY + 2, blocked)
                || collision(tuxX + tuxWidth + fallingSpeed, tuxY + tuxHeight - 2, blocked)))
        {
            fall(delta);
        } else    //when not falling
        {
            gravitySpeed = 0;
        }

        ////Death event
        int collX = 11; //A QUICK AND DIRTY COLLISION FIX
        int collY = 21;
        if (inputDelay.isTimeElapsed() && (collision(tuxX + collX, tuxY + collY, deadly)
                || collision(tuxX + tuxWidth - collX, tuxY + collY, deadly)
                || collision(tuxX + collX, tuxY + tuxHeight - collY, deadly)
                || collision(tuxX + tuxWidth - collX, tuxY + tuxHeight - collY, deadly)))
        {
            die.play(); //sounds sterben, sleep
            try
            {
                Thread.sleep(400);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            tuxX = 79;  //puts tux to default position
            tuxY = 518;
            gravity = 'b';
            tux = bottomStanding;
            tuxWidth = 23;
            tuxHeight = 42;
            inputDelay.reset();
        }

        ////level done event
        if (inputDelay.isTimeElapsed() && (collision(tuxX + 1, tuxY + 1, levelEnd)
                || collision(tuxX + tuxWidth - 1, tuxY + 1, levelEnd)
                || collision(tuxX + 1, tuxY + tuxHeight - 1, levelEnd)
                || collision(tuxX + tuxWidth - 1, tuxY + tuxHeight - 1, levelEnd)))
        {
            if (levelCurrent + 1 < levelMax)
            {
                levelCurrent++;    //loads new level
                win.play();        //sounds win play,
                try                //sleep (dirty...)
                {
                    Thread.sleep(900);
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                AL.destroy();
                gc.reinit();
            } else
            {
                System.out.println("You finished the game Congratulations!");
            }
            inputDelay.reset();
        }

        ////Storm rotation event
        if (gravityTimer.isTimeElapsed() && collision(tuxX + tuxWidth / 2, tuxY + tuxHeight / 2, storm))
        {
            rotateGravity();
            storms.play();
        }

        ////escape key hit for game menu
        if (input.isKeyDown(Input.KEY_ESCAPE) || (((posX > 710 && posX < 800) && (posY > (600 - 32) && posY < (600 - 5))) && Mouse.isButtonDown(0)))
        {
            sbg.enterState(0);
            try
            {
                Thread.sleep(250);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    ////check if collision happened with any object
    private boolean collision(float x, float y, boolean[][] z)
    {
        int xBlock = 0;
        int yBlock = 0;

        switch (gravity)
        {
            case 'b':
                xBlock = (int) (x / size);
                yBlock = (int) (y / size);
                break;
            case 't':
                xBlock = (int) (x / size);
                yBlock = (int) (y / size);
                break;
            case 'l':
                xBlock = (int) ((x - 11) / size);
                yBlock = (int) ((y + 10) / size);
                break;
            case 'r':
                xBlock = (int) ((x - 11) / size);
                yBlock = (int) ((y + 10) / size);
                break;
        }
        return z[xBlock][yBlock];
    }

    ////changes tux animation
    private void setAnimation(Input input)
    {
        switch (gravity)    //change walking animation
        {
            case 'b':
                if (input.isKeyDown(Input.KEY_LEFT) || (input.isKeyDown(Input.KEY_A)))
                {
                    tux = bottomMovingLeft;
                }
                if ((input.isKeyDown(Input.KEY_RIGHT) || input.isKeyDown(Input.KEY_D)))
                {
                    tux = bottomMovingRight;
                }
                break;
            case 't':
                if (input.isKeyDown(Input.KEY_LEFT) || (input.isKeyDown(Input.KEY_A)))
                {
                    tux = topMovingLeft;
                }
                if ((input.isKeyDown(Input.KEY_RIGHT) || input.isKeyDown(Input.KEY_D)))
                {
                    tux = topMovingRight;
                }
                break;
            case 'l':
                if (input.isKeyDown(Input.KEY_UP) || (input.isKeyDown(Input.KEY_W)))
                {
                    tux = leftMovingUp;
                }
                if ((input.isKeyDown(Input.KEY_DOWN) || input.isKeyDown(Input.KEY_S)))
                {
                    tux = leftMovingDown;
                }
                break;
            case 'r':
                if (input.isKeyDown(Input.KEY_UP) || (input.isKeyDown(Input.KEY_W)))
                {
                    tux = rightMovingUp;
                }
                if ((input.isKeyDown(Input.KEY_DOWN) || input.isKeyDown(Input.KEY_S)))
                {
                    tux = rightMovingDown;
                }
                break;
        }
    }

    ////moves tux
    private void moveTux(float x, float y, int delta)
    {
        int offsetX = 0;    //offset for moving right (usually 23)
        int offsetY = 0;    //offset for moving down (usually 42)
        int offsetSize = 0; //offset for collision right. It equals block size (40)

        if (x > 0)  //enables offset for moving right
        {
            offsetX = tuxWidth;
            offsetSize = 39;
        }

        if (y > 0)  //enables offset for moving down
        {
            offsetY = tuxHeight;
        }

        //tux moves left or right
        if (!(collision(tuxX + offsetX + x, tuxY, blocked) || collision(tuxX + offsetX + x, tuxY + tuxHeight - 1, blocked)))
        {
            tux.update(delta);
            tuxX += x;
        } else
        {
            tux.update(delta);
            tuxX = tuxX - ((tuxX + offsetX) % size) + offsetSize;
        }

        //tux moves up or down
        if (!(collision(tuxX, tuxY + offsetY + y, blocked) || collision(tuxX + tuxWidth - 1, tuxY + offsetY + y, blocked)))
        {
            tux.update(delta);
            tuxY += y;
        }
    }

    ////flips gravity
    private void flipGravity()
    {
        switch (gravity)
        {
            case 'b':
                gravity = 't';
                tux = topStanding;
                break;
            case 't':
                gravity = 'b';
                tux = bottomStanding;
                break;
            case 'l':
                gravity = 'r';
                tux = rightStanding;
                break;
            case 'r':
                gravity = 'l';
                tux = leftStanding;
                break;
        }
        inputDelay.reset();
    }

    ////rotates gravity by 90 degree
    private void rotateGravity()
    {
        int tmpSize = tuxWidth;

        switch (gravity)
        {
            case 'b':
                tuxY += 20;
                gravity = 'l';
                tux = leftStanding;
                tuxWidth = tuxHeight; //rotates the image dimensions when gravity is rotated
                tuxHeight = tmpSize;
                break;
            case 't':
                tuxY -= 20;
                gravity = 'r';
                tux = rightStanding;
                tuxWidth = tuxHeight;
                tuxHeight = tmpSize;
                break;
            case 'l':
                tuxX -= 20;
                gravity = 't';
                tux = topStanding;
                tuxWidth = tuxHeight;
                tuxHeight = tmpSize;
                break;
            case 'r':
                tuxX += 20;
                gravity = 'b';
                tux = topStanding;
                tuxWidth = tuxHeight;
                tuxHeight = tmpSize;
                break;
        }
        gravityTimer.reset();
    }

    ////handles falling
    private void fall(int delta)
    {
        ////accelerate falling
        gravitySpeed += gravityAcc * delta;

        ////limit falling speed
        if (gravitySpeed > gravitySpeedMax)
        {
            gravitySpeed = gravitySpeedMax;
        }

        switch (gravity)
        {
            case 'b':
                tux.update(delta);
                tuxY += gravitySpeed;
                break;
            case 't':
                tux.update(delta);
                tuxY -= gravitySpeed;
                break;
            case 'l':
                tux.update(delta);
                tuxX -= gravitySpeed;
                break;
            case 'r':
                tux.update(delta);
                tuxX += gravitySpeed;
                break;
        }
    }

    ////get state ID
    @Override
    public int getID()
    {
        return 1;
    }
}