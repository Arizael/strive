package de.arizael.strive.entities;

import java.text.MessageFormat;

import de.arizael.strive.GameManager;
import de.arizael.strive.entities.Player.PlayerState;
import de.arizael.strive.ui.IngameScreen;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.annotation.CollisionInfo;
import de.gurkenlabs.litiengine.annotation.CombatInfo;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.gui.SpeechBubble;
import de.gurkenlabs.litiengine.gui.SpeechBubbleListener;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ArrayUtilities;

@EntityInfo(width = 11, height = 20)
@CollisionInfo(collisionBoxWidth = 5, collisionBoxHeight = 8, collision = true)
@CombatInfo(isIndestructible = true)
public class Gatekeeper extends Creature {
  public static final String MESSAGE_FINISH = "FINISH";
  private static final String[] rudeLines;
  private static final String[] goalLines;

  private int requiredSlaves;
  private String nextLevel;

  private boolean transitioning;

  static {
    rudeLines = Resources.strings().getList("rude.txt");
    goalLines = Resources.strings().getList("goal.txt");
  }

  public Gatekeeper() {
    this.addMessageListener(l -> {
      if (l.getMessage() == null) {
        return;
      }

      if (l.getMessage().equals(MESSAGE_FINISH)) {

        String text = String.format("You need to trade me %d slave%s or I won't help you travel to Rome.", this.requiredSlaves, (this.requiredSlaves > 1 ? "s" : ""));
        if (GameManager.getOwnSlaveCount() >= this.getRequiredSlaves()) {
          this.setTransitioning(true);
          text = String.format("WELL DONE! Now I can take you to %s.", GameManager.getCity(this.getNextLevel()));

          Game.audio().playSound(Resources.sounds().get("success.ogg"));
          SpeechBubble bubble = SpeechBubble.create(this, text, GameManager.SPEECH_BUBBLE_APPEARANCE, GameManager.SPEECH_BUBBLE_FONT);
          bubble.setTextDisplayTime(4000);
          Player.instance().setState(PlayerState.LOCKED);

          bubble.addListener(new SpeechBubbleListener() {
            @Override
            public void hidden() {

              Game.window().getRenderComponent().fadeOut(1000);

              Game.loop().perform(1500, () -> {
                // remove player before unloading the environment or the instance's animation controller will be disposed
                Game.world().environment().remove(Player.instance());
                Game.world().loadEnvironment(getNextLevel());
                setTransitioning(false);
              });
            }
          });
        } else {
          SpeechBubble.create(this, text, GameManager.SPEECH_BUBBLE_APPEARANCE, GameManager.SPEECH_BUBBLE_FONT);
        }
      }
    });
  }

  public int getRequiredSlaves() {
    return requiredSlaves;
  }

  public void setRequiredSlaves(int requiredSlaves) {
    this.requiredSlaves = requiredSlaves;
  }

  @Override
  public void loaded(Environment environment) {
    super.loaded(environment);

    Game.loop().perform(1000, () -> {
      if (Game.world().environment().getMap().getName().equals("level0")) {
        performIntroduction();
        return;
      }
      String rude = ArrayUtilities.getRandom(rudeLines);

      String goal = MessageFormat.format(ArrayUtilities.getRandom(goalLines), GameManager.getCurrentCity(), this.getRequiredSlaves());
      SpeechBubble bubble = SpeechBubble.create(this, rude + " " + goal, GameManager.SPEECH_BUBBLE_APPEARANCE, GameManager.SPEECH_BUBBLE_FONT);
      bubble.setTextDisplayTime(5000);
      bubble.addListener(new SpeechBubbleListener() {
        @Override
        public void hidden() {
          Player.instance().setState(PlayerState.CONTROLLABLE);
          IngameScreen.levelNameTick = Game.time().now();
        }
      });
    });
  }

  private void performIntroduction() {

    SpeechBubble bubble1 = SpeechBubble.create(this, String.format("%s is a terrible dust pit. Let us seek fortune together in Rome, the heart of this glorious empire!", Game.world().environment().getMap().getStringValue(MapProperty.MAP_TITLE)), GameManager.SPEECH_BUBBLE_APPEARANCE,
        GameManager.SPEECH_BUBBLE_FONT);
    bubble1.setTextDisplayTime(6500);
    bubble1.addListener(new SpeechBubbleListener() {
      @Override
      public void hidden() {

        SpeechBubble bubble2 = SpeechBubble.create(Gatekeeper.this, "First, you need to prove your worth to me!", GameManager.SPEECH_BUBBLE_APPEARANCE, GameManager.SPEECH_BUBBLE_FONT);
        bubble2.setTextDisplayTime(4500);
        bubble2.addListener(new SpeechBubbleListener() {
          @Override
          public void hidden() {
            SpeechBubble bubble3 = SpeechBubble.create(Gatekeeper.this, "Bring me a slave and I will help you travel to a more glorious place.", GameManager.SPEECH_BUBBLE_APPEARANCE, GameManager.SPEECH_BUBBLE_FONT);
            bubble3.setTextDisplayTime(5500);
            bubble3.addListener(new SpeechBubbleListener() {
              @Override
              public void hidden() {

                SpeechBubble bubble4 = SpeechBubble.create(Gatekeeper.this, "Maybe you could ask that Roman soldier over there nicely to hand over his slave to you.", GameManager.SPEECH_BUBBLE_APPEARANCE, GameManager.SPEECH_BUBBLE_FONT);
                bubble4.setTextDisplayTime(6500);
                bubble4.addListener(new SpeechBubbleListener() {
                  @Override
                  public void hidden() {
                    Player.instance().setState(PlayerState.CONTROLLABLE);
                  }
                });
              }
            });
          }
        });
      }
    });

  }

  public String getNextLevel() {
    return nextLevel;
  }

  public void setNextLevel(String nextLevel) {
    this.nextLevel = nextLevel;
  }

  public boolean isTransitioning() {
    return transitioning;
  }

  public void setTransitioning(boolean transitioning) {
    this.transitioning = transitioning;
  }
}
