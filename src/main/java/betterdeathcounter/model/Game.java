package betterdeathcounter.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;
import java.beans.PropertyChangeSupport;

public class Game
{
   public static final String PROPERTY_NAME = "name";
   public static final String PROPERTY_BOSSES = "bosses";
   public static final String PROPERTY_PLAYER = "player";
   public static final String PROPERTY_SPREADSHEET_ID = "spreadsheetId";
   private String name;
   private List<Boss> bosses;
   private Player player;
   protected PropertyChangeSupport listeners;
   private String spreadsheetId;

   public String getName()
   {
      return this.name;
   }

   public Game setName(String value)
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

   public List<Boss> getBosses()
   {
      return this.bosses != null ? Collections.unmodifiableList(this.bosses) : Collections.emptyList();
   }

   public Game withBosses(Boss value)
   {
      if (this.bosses == null)
      {
         this.bosses = new ArrayList<>();
      }
      if (!this.bosses.contains(value))
      {
         this.bosses.add(value);
         value.setGame(this);
         this.firePropertyChange(PROPERTY_BOSSES, null, value);
      }
      return this;
   }

   public Game withBosses(Boss... value)
   {
      for (final Boss item : value)
      {
         this.withBosses(item);
      }
      return this;
   }

   public Game withBosses(Collection<? extends Boss> value)
   {
      for (final Boss item : value)
      {
         this.withBosses(item);
      }
      return this;
   }

   public Game withoutBosses(Boss value)
   {
      if (this.bosses != null && this.bosses.remove(value))
      {
         value.setGame(null);
         this.firePropertyChange(PROPERTY_BOSSES, value, null);
      }
      return this;
   }

   public Game withoutBosses(Boss... value)
   {
      for (final Boss item : value)
      {
         this.withoutBosses(item);
      }
      return this;
   }

   public Game withoutBosses(Collection<? extends Boss> value)
   {
      for (final Boss item : value)
      {
         this.withoutBosses(item);
      }
      return this;
   }

   public Player getPlayer()
   {
      return this.player;
   }

   public Game setPlayer(Player value)
   {
      if (this.player == value)
      {
         return this;
      }

      final Player oldValue = this.player;
      if (this.player != null)
      {
         this.player = null;
         oldValue.withoutGames(this);
      }
      this.player = value;
      if (value != null)
      {
         value.withGames(this);
      }
      this.firePropertyChange(PROPERTY_PLAYER, oldValue, value);
      return this;
   }

   public String getSpreadsheetId()
   {
      return this.spreadsheetId;
   }

   public Game setSpreadsheetId(String value)
   {
      if (Objects.equals(value, this.spreadsheetId))
      {
         return this;
      }

      final String oldValue = this.spreadsheetId;
      this.spreadsheetId = value;
      this.firePropertyChange(PROPERTY_SPREADSHEET_ID, oldValue, value);
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
      result.append(' ').append(this.getSpreadsheetId());
      return result.substring(1);
   }

   public void removeYou()
   {
      this.withoutBosses(new ArrayList<>(this.getBosses()));
      this.setPlayer(null);
   }
}
