package betterdeathcounter.model;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.Collection;
import java.beans.PropertyChangeSupport;

public class Boss
{
   public static final String PROPERTY_NAME = "name";
   public static final String PROPERTY_GAME = "game";
   public static final String PROPERTY_SECOND_PHASE = "secondPhase";
   public static final String PROPERTY_PREDICTION = "prediction";
   public static final String PROPERTY_DEATHS = "deaths";
   private String name;
   private Game game;
   protected PropertyChangeSupport listeners;
   private Boolean secondPhase;
   private double[] prediction;
   private List<Death> deaths;

   public String getName()
   {
      return this.name;
   }

   public Boss setName(String value)
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

   public Game getGame()
   {
      return this.game;
   }

   public Boss setGame(Game value)
   {
      if (this.game == value)
      {
         return this;
      }

      final Game oldValue = this.game;
      if (this.game != null)
      {
         this.game = null;
         oldValue.withoutBosses(this);
      }
      this.game = value;
      if (value != null)
      {
         value.withBosses(this);
      }
      this.firePropertyChange(PROPERTY_GAME, oldValue, value);
      return this;
   }

   public Boolean getSecondPhase()
   {
      return this.secondPhase;
   }

   public Boss setSecondPhase(Boolean value)
   {
      if (Objects.equals(value, this.secondPhase))
      {
         return this;
      }

      final Boolean oldValue = this.secondPhase;
      this.secondPhase = value;
      this.firePropertyChange(PROPERTY_SECOND_PHASE, oldValue, value);
      return this;
   }

   public double[] getPrediction()
   {
      return this.prediction;
   }

   public Boss setPrediction(double[] value)
   {
      if (Objects.equals(value, this.prediction))
      {
         return this;
      }

      final double[] oldValue = this.prediction;
      this.prediction = value;
      this.firePropertyChange(PROPERTY_PREDICTION, oldValue, value);
      return this;
   }

   public List<Death> getDeaths()
   {
      return this.deaths != null ? Collections.unmodifiableList(this.deaths) : Collections.emptyList();
   }

   public Boss withDeaths(Death value)
   {
      if (this.deaths == null)
      {
         this.deaths = new ArrayList<>();
      }
      if (!this.deaths.contains(value))
      {
         this.deaths.add(value);
         this.firePropertyChange(PROPERTY_DEATHS, null, value);
      }
      return this;
   }

   public Boss withDeaths(Death... value)
   {
      for (final Death item : value)
      {
         this.withDeaths(item);
      }
      return this;
   }

   public Boss withDeaths(Collection<? extends Death> value)
   {
      for (final Death item : value)
      {
         this.withDeaths(item);
      }
      return this;
   }

   public Boss withoutDeaths(Death value)
   {
      if (this.deaths != null && this.deaths.remove(value))
      {
         this.firePropertyChange(PROPERTY_DEATHS, value, null);
      }
      return this;
   }

   public Boss withoutDeaths(Death... value)
   {
      for (final Death item : value)
      {
         this.withoutDeaths(item);
      }
      return this;
   }

   public Boss withoutDeaths(Collection<? extends Death> value)
   {
      for (final Death item : value)
      {
         this.withoutDeaths(item);
      }
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
      this.setGame(null);
      this.withoutDeaths(new ArrayList<>(this.getDeaths()));
   }
}
