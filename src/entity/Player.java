package entity;

import game.GamePanel;
import game.GestioneTasti;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import tile.TileManager;
import utils.Defines;
import utils.Drawable;
import utils.Spritesheet;
import utils.SpritesheetEntity;
import utils.Updateable;

public class Player extends Entity implements Drawable, Updateable{
    private final GestioneTasti gestioneTasti;

    private int worldX;
    private int worldY;
    private int screenX;
    private int screenY;

    boolean isAlive;
    boolean isAliveAnimation = false;

    SpritesheetEntity moving;
    SpritesheetEntity notMoving;
    Spritesheet dying;
    
    BufferedImage image = null;
    //Costruttore

    public Player(GestioneTasti g2) {
        gestioneTasti = g2;

        screenX = Defines.SCREEN_WIDTH / 2 - Defines.GRANDEZZA_CASELLE / 2;
        screenY = Defines.SCREEN_HEIGHT / 2 - Defines.GRANDEZZA_CASELLE / 2;
        setDefaultValues();
    }

    //Aggiornamento del personaggio
    @Override
    public void update() {
        areaCollisione.width = Defines.GRANDEZZA_CASELLE - 20;
        areaCollisione.height = Defines.GRANDEZZA_CASELLE - 24;
        
        checkIfKilled();
        checkIfRevived();
        checkIfDead();
        checkIfAlive();
    }

    //Disegno del personaggio
    @Override
    public void draw(Graphics2D g) {
        setImage();
        setScreenCoord();
        Defines.CAMERA.update();
        drawPlayer(g);
    }
    
    // Getters

    public int getWorldX() {
        return worldX;
    }

    public int getWorldY() {
        return worldY;
    }

    public void setWorldX(int n) {
        worldX = n;
    }

    public int getScreenX() {
        return this.screenX;
    }

    public int getScreenY() {
        return this.screenY;
    }

    protected int getCol(){
        return worldX/Defines.GRANDEZZA_CASELLE;
    }

    protected int getRow(){
        return worldY/Defines.GRANDEZZA_CASELLE;
    }

    private void getPlayerImage() {
        String path = "/res/player/";
        moving = new SpritesheetEntity(4, 5, 3, 4, path, "finn.png");
        notMoving = new SpritesheetEntity(2, 2, 0, 1, path, "finn.png");
        dying = new Spritesheet(6, 0, "/res/player/","FinnDeath.png");
    }
    
    // Setters

    private boolean getPremuto(String key) {
        return gestioneTasti.getPremuto(key);
    }

    public void setWorldY(int n) {
        worldY = n;
    }
    

    private void setDefaultValues() {
        isAlive = true;
        setDirezione("su");
        getPlayerImage();
        areaCollisione = new Rectangle();
        areaCollisione.x = 10;
        areaCollisione.y = 20;
        areaCollisione.width = Defines.GRANDEZZA_CASELLE - 20;
        areaCollisione.height = Defines.GRANDEZZA_CASELLE - 24;
    }

   
    // Tempo di attesa per cambiare sprite
    private void spriteCounter(int n, int wait) {
        this.spriteCounter++;
        if (this.spriteCounter > wait * Defines.FPS / 60) {
            if (spriteNum < n) {
                spriteNum++;
            } else {
                spriteNum = 0;
            }
            this.spriteCounter = 0;
        }
    }

    //Verifica se il personaggio è vicino ai bordi
    private boolean lontanoDaiBordi() {
        boolean stato = false;
        int offset = 1;
        if (getCol() < offset || getCol() > GamePanel.getMaxWorldCol() - 2 - offset) {
            stato = true;
        }
        if (getRow() < offset || getRow() > GamePanel.getMaxWorldRow() - 2 - offset) {
            stato = true;
        }
        return stato;
    }
    
    // Movimento del personaggio
    private void spostaX(String direction, String operazione) {
        setDirezione(direction);
        if (operazione.equals("aggiungi"))
            worldX += speed;
        if (operazione.equals("sottrai"))
            worldX -= speed;
    }

    private void spostaY(String direction, String operazione) {
        setDirezione(direction);
        if (operazione.equals("aggiungi"))
            worldY += speed;
        if (operazione.equals("sottrai"))
            worldY -= speed;
    }

    public void checkIfKilled(){
        boolean k = getPremuto("K");

        if (isAlive && k) {
            isAlive = false;
            isAliveAnimation=false;
        }
    }

    public void checkIfRevived(){
        boolean e = getPremuto("E");
        
        if (e && !isAlive) {
            spriteCounter(0, 0);
            isAlive=true;
            isAliveAnimation=true;
        }
    }

    public void checkIfDead(){
        if (!isAlive && !isAliveAnimation) {
            spriteCounter(5, 10);
            if (spriteNum==5) {
                spriteNum=4;
            }
        }
    }
    public boolean inDiagonale(){
        boolean w = getPremuto("W");
        boolean s = getPremuto("S");
        boolean d = getPremuto("D");
        boolean a = getPremuto("A");
        return (w && d) || (w && a) || (s && d) || (s && a);
    }
    public void checkIfAlive(){
        if (isAlive) {
            boolean w = getPremuto("W");
            boolean s = getPremuto("S");
            boolean d = getPremuto("D");
            boolean a = getPremuto("A");

            boolean shift = getPremuto("SHIFT");
            if (shift ) {
                
                if (!lontanoDaiBordi() && !TileManager.ambienteAperto) {
                    setSpeed(2, 1.5);
                }else setSpeed(2, 1);
                
            }else if (!shift){
                setSpeed(2, 1);
            }
          
                
            
    
            if (!w && !s && !d && !a) {
                spriteCounter(1, 27);
                switch (getDirezione()) {
                    case "giu" -> {
                        spostaY("fermoGiu", "");
                        return;
                    }
                    case "su" -> {
                        spostaY("fermoSu", "");
                        return;
                    }
                    case "destra" -> {
                        spostaX("fermoDestra", "");
                        return;
                    }
                    case "sinistra" -> {
                        spostaX("fermoSinistra", "");
                        return;
                    }
                    default -> {
                        return;
                    }
                }
            }
            try {
                if (w || s || d || a) {
                    spriteCounter(3, 8);
                    if (w && s && a && d){
                        setDirezione("fermoGiu");
                    }else if (a && d) {
                        setDirezione("fermoGiu");
                        if (w)
                            setDirezione("su");
                        if (s)
                            setDirezione("giu");
                        controllaCollisioniY(w, s);
                    } else if (w && s) {
                        setDirezione("fermoGiu");
                        if (d)
                            setDirezione("destra");
                        if (a)
                            setDirezione("sinistra");
                        controllaCollisioniX(a, d);
                    }else{
                        if (w)
                            setDirezione("su");
                        if (s)
                            setDirezione("giu");
                        controllaCollisioniY(w, s);
                        if (d)
                            setDirezione("destra");
                        if (a)
                            setDirezione("sinistra");
                        // CONTROLLA COLLISIONI
                        controllaCollisioniX(a, d);
                    }
                }
            } catch (Exception e2) {
                System.err.println("Errore: personaggio fuori dalla mappa");
            }
        }
    }

    //Controlla le collisioni del personaggio
    
    public void controllaCollisioniX(boolean a, boolean d) {
        inCollisione = false;
        Defines.GAME_PANEL.collisioni.controllaCasella(this);
        if (!inCollisione) {
            if (d)
                spostaX("destra", "aggiungi");
            if (a)
                spostaX("sinistra", "sottrai");

        }
    }

    public void controllaCollisioniY(boolean w, boolean s) {
        inCollisione = false;
        Defines.GAME_PANEL.collisioni.controllaCasella(this);
        if (!inCollisione) {
            if (w)
                spostaY("su", "sottrai");
            if (s)
                spostaY("giu", "aggiungi");
        }
    }

    //CAMBIO IMMAGINE A SECONDA DELLE AZIONI

    public void setImage(){
        if (isAlive) {
            switch (getDirezione()) {
                case "su" -> image = moving.getUp().getSpriteSheet(spriteNum);
                case "giu" -> image = moving.getDown().getSpriteSheet(spriteNum);
                case "destra" -> image = moving.getRight().getSpriteSheet(spriteNum);
                case "sinistra" -> image = moving.getLeft().getSpriteSheet(spriteNum);
                case "fermoDestra" -> {
                    if (spriteNum == 1)
                        image = notMoving.getRight().getSpriteSheet(0);
                    else
                        image = notMoving.getRight().getSpriteSheet(1);
                }
                case "fermoSinistra" -> {
                    if (spriteNum == 1)
                        image = notMoving.getLeft().getSpriteSheet(0);
                    else
                        image = notMoving.getLeft().getSpriteSheet(1);
                }
                case "fermoSu" -> {
                    if (spriteNum == 1)
                        image = notMoving.getUp().getSpriteSheet(0);
                    else
                        image = notMoving.getUp().getSpriteSheet(1);
                }
                case "fermoGiu" -> {
                    if (spriteNum == 1)
                        image = notMoving.getDown().getSpriteSheet(0);
                    else
                        image = notMoving.getDown().getSpriteSheet(1);
                }
    
            }
        }else{
            image = dying.getSpriteSheet(spriteNum);
        }
    }

    public void setScreenCoord(){
        if(TileManager.ambienteAperto){
            screenX=worldX;
            screenY=worldY;
        }else{
            screenX = Defines.SCREEN_WIDTH / 2 - Defines.GRANDEZZA_CASELLE / 2;
            screenY = Defines.SCREEN_HEIGHT / 2 - Defines.GRANDEZZA_CASELLE / 2;
        }
    }

    public void translateTemp(int x, int y, Graphics2D g){
        int temp = Defines.SCREEN_WIDTH/2 - GamePanel.getMaxWorldCol()*Defines.GRANDEZZA_CASELLE/2;
        g.translate(x+temp, y);
    }
    
    public void untranslateTemp(int x, int y, Graphics2D g){
        int temp = Defines.SCREEN_WIDTH/2 - GamePanel.getMaxWorldCol()*Defines.GRANDEZZA_CASELLE/2;
        g.translate(-x-temp, -y);
    }

    public void translate(int x, int y, Graphics2D g){
        g.translate(x, y);
    }

    public void untranslate(int x, int y, Graphics2D g){
        g.translate(-x, -y);
    }

    public void drawPlayer(Graphics2D g){
        if (TileManager.ambienteAperto) {

            //STAMPA PLAYER
            translateTemp(screenX - Defines.GRANDEZZA_CASELLE / 2,  screenY - Defines.GRANDEZZA_CASELLE / 2, g);
            g.drawImage(image, 0, 0, Defines.GRANDEZZA_CASELLE*2, Defines.GRANDEZZA_CASELLE*2, null);
            untranslateTemp(screenX - Defines.GRANDEZZA_CASELLE / 2,  screenY - Defines.GRANDEZZA_CASELLE / 2, g);
            
            //STAMPA RETTANGOLO COLLISIONI
            translateTemp(screenX + areaCollisione.x, screenY + areaCollisione.y, g);
            //g.drawRect(0 , 0 , areaCollisione.width, areaCollisione.height);
            untranslateTemp(screenX + areaCollisione.x, screenY + areaCollisione.y, g);

        }else{

            //STAMPA PLAYER
            translate(screenX - Defines.GRANDEZZA_CASELLE / 2, screenY - Defines.GRANDEZZA_CASELLE / 2, g);
            g.drawImage(image, 0, 0, Defines.GRANDEZZA_CASELLE * 2, Defines.GRANDEZZA_CASELLE * 2, null);
            untranslate(screenX - Defines.GRANDEZZA_CASELLE / 2, screenY - Defines.GRANDEZZA_CASELLE / 2, g);

            //STAMPA RETTANGOLO COLLISIONI
            translate(screenX + areaCollisione.x, screenY + areaCollisione.y, g);
            //g.drawRect(0, 0, areaCollisione.width, areaCollisione.height);
            untranslate(screenX + areaCollisione.x, screenY + areaCollisione.y, g);
            
        }
    }
}
