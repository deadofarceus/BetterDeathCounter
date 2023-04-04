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
   public static final String PROPERTY_SETTINGS = "settings";
   public static final String PROPERTY_CURRENT_GAME = "currentGame";
   public static final String PROPERTY_CURRENT_BOSS = "currentBoss";
   private String name;
   private List<Game> games;
   protected PropertyChangeSupport listeners;
   private Settings settings;
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

   public Settings getSettings()
   {
      return this.settings;
   }

   public Player setSettings(Settings value)
   {
      if (this.settings == value)
      {
         return this;
      }

      final Settings oldValue = this.settings;
      this.settings = value;
      this.firePropertyChange(PROPERTY_SETTINGS, oldValue, value);
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
      return result.substring(1);
   }

   public void removeYou()
   {
      this.withoutGames(new ArrayList<>(this.getGames()));
      this.setSettings(null);
      this.setCurrentGame(null);
      this.setCurrentBoss(null);
   }
}
