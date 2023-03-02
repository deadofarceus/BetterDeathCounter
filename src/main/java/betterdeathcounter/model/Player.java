package betterdeathcounter.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;
import java.beans.PropertyChangeSupport;

public class Player
{
   public static final String PROPERTY_NAME = "name";
   public static final String PROPERTY_GAMES = "games";
   public static final String PROPERTY_GARBAGE_FACTOR = "garbageFactor";
   public static final String PROPERTY_APIUSERNAME = "APIUsername";
   public static final String PROPERTY_SHOW_LINEAR = "showLinear";
   public static final String PROPERTY_SHOW_EXP = "showExp";
   public static final String PROPERTY_SHOW_TIMER = "showTimer";
   public static final String PROPERTY_CURRENT_GAME = "currentGame";
   public static final String PROPERTY_CURRENT_BOSS = "currentBoss";
   private String name;
   private List<Game> games;
   protected PropertyChangeSupport listeners;
   private double garbageFactor;
   private String APIUsername;
   private Boolean showLinear;
   private Boolean showExp;
   private Boolean showTimer;
   private Game currentGame;
   private Boss currentBoss;

   public String getName()
   {
      return this.name;
   }

   public Player setName(String value)
   {
      if (Objects.equals(value, this.name))
      {
         return this;
      }

      final String oldValue = this.name;
      this.name = value;
      this.firePropertyChange(PROPERTY_NAME, oldValue, value);
      return this;
   }

   public List<Game> getGames()
   {
      return this.games != null ? Collections.unmodifiableList(this.games) : Collections.emptyList();
   }

   public Player withGames(Game value)
   {
      if (this.games == null)
      {
         this.games = new ArrayList<>();
      }
      if (!this.games.contains(value))
      {
         this.games.add(value);
         value.setPlayer(this);
         this.firePropertyChange(PROPERTY_GAMES, null, value);
      }
      return this;
   }

   public Player withGames(Game... value)
   {
      for (final Game item : value)
      {
         this.withGames(item);
      }
      return this;
   }

   public Player withGames(Collection<? extends Game> value)
   {
      for (final Game item : value)
      {
         this.withGames(item);
      }
      return this;
   }

   public Player withoutGames(Game value)
   {
      if (this.games != null && this.games.remove(value))
      {
         value.setPlayer(null);
         this.firePropertyChange(PROPERTY_GAMES, value, null);
      }
      return this;
   }

   public Player withoutGames(Game... value)
   {
      for (final Game item : value)
      {
         this.withoutGames(item);
      }
      return this;
   }

   public Player withoutGames(Collection<? extends Game> value)
   {
      for (final Game item : value)
      {
         this.withoutGames(item);
      }
      return this;
   }

   public double getGarbageFactor()
   {
      return this.garbageFactor;
   }

   public Player setGarbageFactor(double value)
   {
      if (value == this.garbageFactor)
      {
         return this;
      }

      final double oldValue = this.garbageFactor;
      this.garbageFactor = value;
      this.firePropertyChange(PROPERTY_GARBAGE_FACTOR, oldValue, value);
      return this;
   }

   public String getAPIUsername()
   {
      return this.APIUsername;
   }

   public Player setAPIUsername(String value)
   {
      if (Objects.equals(value, this.APIUsername))
      {
         return this;
      }

      final String oldValue = this.APIUsername;
      this.APIUsername = value;
      this.firePropertyChange(PROPERTY_APIUSERNAME, oldValue, value);
      return this;
   }

   public Boolean getShowLinear()
   {
      return this.showLinear;
   }

   public Player setShowLinear(Boolean value)
   {
      if (Objects.equals(value, this.showLinear))
      {
         return this;
      }

      final Boolean oldValue = this.showLinear;
      this.showLinear = value;
      this.firePropertyChange(PROPERTY_SHOW_LINEAR, oldValue, value);
      return this;
   }

   public Boolean getShowExp()
   {
      return this.showExp;
   }

   public Player setShowExp(Boolean value)
   {
      if (Objects.equals(value, this.showExp))
      {
         return this;
      }

      final Boolean oldValue = this.showExp;
      this.showExp = value;
      this.firePropertyChange(PROPERTY_SHOW_EXP, oldValue, value);
      return this;
   }

   public Boolean getShowTimer()
   {
      return this.showTimer;
   }

   public Player setShowTimer(Boolean value)
   {
      if (Objects.equals(value, this.showTimer))
      {
         return this;
      }

      final Boolean oldValue = this.showTimer;
      this.showTimer = value;
      this.firePropertyChange(PROPERTY_SHOW_TIMER, oldValue, value);
      return this;
   }

   public Game getCurrentGame()
   {
      return this.currentGame;
   }

   public Player setCurrentGame(Game value)
   {
      if (this.currentGame == value)
      {
         return this;
      }

      final Game oldValue = this.currentGame;
      this.currentGame = value;
      this.firePropertyChange(PROPERTY_CURRENT_GAME, oldValue, value);
      return this;
   }

   public Boss getCurrentBoss()
   {
      return this.currentBoss;
   }

   public Player setCurrentBoss(Boss value)
   {
      if (this.currentBoss == value)
      {
         return this;
      }

      final Boss oldValue = this.currentBoss;
      this.currentBoss = value;
      this.firePropertyChange(PROPERTY_CURRENT_BOSS, oldValue, value);
      return this;
   }

   public boolean firePropertyChange(String propertyName, Object oldValue, Object newValue)
   {
      if (this.listeners != null)
      {
         this.listeners.firePropertyChange(propertyName, oldValue, newValue);
         return true;
      }
      return false;
   }

   public PropertyChangeSupport listeners()
   {
      if (this.listeners == null)
      {
         this.listeners = new PropertyChangeSupport(this);
      }
      return this.listeners;
   }

   @Override
   public String toString()
   {
      final StringBuilder result = new StringBuilder();
      result.append(' ').append(this.getName());
      result.append(' ').append(this.getAPIUsername());
      return result.substring(1);
   }

   public void removeYou()
   {
      this.setCurrentGame(null);
      this.setCurrentBoss(null);
      this.withoutGames(new ArrayList<>(this.getGames()));
   }
}
