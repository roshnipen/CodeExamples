package ooga.engine.obstacles;

import java.lang.reflect.Method;
import java.util.Iterator;
import javafx.scene.Node;
import javafx.scene.shape.Rectangle;
import ooga.engine.entities.Entity;
import ooga.engine.games.Collideable;
import ooga.engine.games.GamePropertyFileReader;

/**
 * Class to take care of the obstalces in different games. Extends rectangle and has an associated
 * node. Implements collideable as items can collide with them
 * Having the Collideable methods overridden in subclasses to create unique collision behavior is repetitive and the duplication way too much. To solve
 * this issue we decided to remove all of this unnecessary duplication and also switch to more data driven code. We did this by using property files
 * that would hold all of the collision action methods and parameters that could be parsed through to create a reflection method that would
 * automatically call all of the necessary collision action methods. This would mean that these 4 Collideable methods above would only need to appear 
 * in the top abstract class, Obstacle. And from the abstract superclass, this Collideable methods could all call the reflection invokeMethod that 
 * would, from the properties file, find out which collision action method/methods should be called to create the correct obstacle collision method. 
 */
public abstract class Obstacle extends Rectangle implements Collideable, Unmovable {

  private static final double NEGATIVE_DIRECTION = -1;
  private static final double SHRINK_RATIO = .7;
  private double normalForce = 0;
  private double moveX = 0;
  private double moveY = 0;
  private double initialX;
  private double initialY;
  private boolean reached;
  private boolean left = false;
  private boolean right = false;

  /**
   * Input constructor for
   *
   * @param obstacleWidth
   * @param obstacleHeight
   * @param initialX
   * @param initialY
   */
  public Obstacle(int obstacleWidth, int obstacleHeight, double initialX, double initialY) {
    this.initialX = initialX;
    this.initialY = initialY;
    reached = false;
    setX(initialX);
    setY(initialY);
    setWidth(obstacleWidth);
    setHeight(obstacleHeight);
  }

  /**
   * Returns the node object
   *
   * @return Node object
   */
  public Node getNode() {
    return (Node) this;
  }

  /**
   * Returns the collidebale which will have less information that obstacles
   *
   * @return upcasts obstacle to collideable
   */
  public Collideable getCollideable() {
    return (Collideable) this;
  }

  /**
   * Moves the location of obstacle by X amount in the x axis. Allows for moving of obstacle
   *
   * @param x input amount to move obstacle by
   */
  public void moveXBy(double x) {
    relocate(getBoundsInParent().getMinX() + x, getBoundsInParent().getMinY());
  }

  /**
   * Moves the location of obstacle by Y amount. Allows for moving of obstacle up and down
   *
   * @param y input amount to move obstacle by
   */
  public void moveYBy(double y) {
    relocate(getBoundsInParent().getMinX(), getBoundsInParent().getMinY() + y);
  }

  /**
   * Old method that previously allowed for non stop moving continously by a certain amount
   *
   * @param X how much to move by in the x direction
   */
  public void moveContinouslyXBy(double X) {
    moveX = X;
  }

  /**
   * Old method that previously allowed for non stop moving continously by a certain amount
   *
   * @param Y how much to move by in y direction
   */
  public void moveContinouslyYBy(double Y) {
    moveY = Y;
  }

  /**
   * Sets the normal upwards force
   *
   * @param gravity input for the amount of upwards normal force
   */
  public void setNormalForce(double gravity) {
    this.normalForce = gravity;
  }

  /**
   * Determines what to do when objects collides on the left. Uses reflection and property file to
   * generalize collision and propoerty file will do and determine the action
   *
   * @param entity: entity to perform collision on
   */
  public void leftCollideable(Entity entity) {
    invokeMethod(entity, "left");
  }

  /**
   * Determines what to do when objects collides on the right. Uses reflection and property file to
   * generalize collision and propoerty file will do and determine the action
   *
   * @param entity: entity to perform collision on
   */
  public void rightCollideable(Entity entity) {
    invokeMethod(entity, "right");
  }

  /**
   * Determines what to do when objects collides on the bottom. Uses reflection and property file to
   * generalize collision and propoerty file will do and determine the action
   *
   * @param entity: entity to perform collision on
   */
  public void bottomCollideable(Entity entity) {
    invokeMethod(entity, "bottom");
  }

  /**
   * Determines what to do when objects collides on the top. Uses reflection and property file to
   * generalize collision and propoerty file will do and determine the action
   *
   * @param entity: entity to perform collision on
   */
  public void topCollideable(Entity entity) {
    invokeMethod(entity, "top");
  }

  /**
   * Method essentially uses information in the property file to determine what method to perform.
   * Doesn't do anything if the property file is bad
   *
   * @param entity what to perform action on
   * @param collisionName what method to call
   */
  protected void invokeMethod(Entity entity, String collisionName) {

    GamePropertyFileReader reader = new GamePropertyFileReader(this.getClass().getSimpleName());
    Iterator methods = reader.getMethods(collisionName).iterator();
    while (methods != null && methods.hasNext()) {
      Class current = this.getClass().getSuperclass();
      try {
        Method x = current.getDeclaredMethod((String) methods.next(), Entity.class);
        x.setAccessible(true);
        x.invoke(this, entity);
      } catch (Exception e) {
        return;
      }
    }
  }
  
  //make entity collision with top side of obstacle collide properly and not fall through obstacle
  private void topCollide(Entity entity) {
    entity.setMaxY(getBoundsInParent().getMinY());
    entity.setYForce(entity.getYForce() + NEGATIVE_DIRECTION * normalForce);
    entity.setVelocityY(0);
    entity.setJump(false);

  }

  /**
   * Sets hitpoints of weapon to 0 and thus by removes the weapon
   * @param entity: entity to remove
   */
  protected void removeWeapon(Entity entity) {
    if (entity.getId().equals("bullet") || entity.getId().equals("arrow")) {
      entity.setHitpoints(0);
    }
  }

  //make entity collision with left side of obstacle collide properly and not be able to go through 
  private void leftCollide(Entity entity) {
    entity.setXForce(0);
    entity.setCenterX(entity.getCenterX() + 1);
    entity.setVelocityX(entity.getVelocityX() * NEGATIVE_DIRECTION);
  }

  //make entity collision with right side of obstacle collide properly and not be able to go through 
  private void rightCollide(Entity entity) {
    entity.setXForce(0);
    entity.setCenterX(entity.getCenterX() - 1);
    entity.setVelocityX(entity.getVelocityX() * NEGATIVE_DIRECTION);
  }

  //make entity collision with bottom side of obstacle collide properly and not be able to go through 
  private void bottomCollide(Entity entity) {
    entity.setVelocityY(0);
  }

  /**
   * When the game has been won, sets this to true
   * @param entity: player entity that will be set to win
   */
  public void wonGame(Entity entity) {
    entity.setWon(true);
  }

  /**
   * Kills player by setting hitpoint to 0
   * @param entity entity to kill
   */
  public void entityDeath(Entity entity) {
    entity.setHitpoints(0);
  }

  /**
   * Scales player by the shrink ratio by setting the height and width
   * @param entity player entity to scale
   */
  //https://stackoverflow.com/questions/24393636/the-pain-with-the-pane-in-javafx-how-can-you-scale-nodes-with-fixed-top-left-co
  public void scalePlayer(Entity entity) {
    if (entity.getId().equals("player")) {
      if (!entity.hasShrunk()) {
        entity.setHeight(entity.getWidth() * SHRINK_RATIO);
        entity.setWidth(entity.getHeight() * SHRINK_RATIO);
        entity.setShrunk(true);
      }
    }
  }

}
