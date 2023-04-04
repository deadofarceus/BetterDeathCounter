package betterdeathcounter.model;
import java.util.Objects;
import java.beans.PropertyChangeSupport;

public class Settings
{
   public static final String PROPERTY_APIUSERNAME = "APIUsername";
   public static final String PROPERTY_GARBAGE_FACTOR = "garbageFactor";
   public static final String PROPERTY_SHOW_LINEAR = "showLinear";
   public static final String PROPERTY_SHOW_EXP = "showExp";
   public static final String PROPERTY_SHOW_TIMER = "showTimer";
   public static final String PROPERTY_CUMULATIVE_PROBABILITY_SCALING = "cumulativeProbabilityScaling";
   public static final String PROPERTY_NUM_BAD_TRYS = "numBadTrys";
   public static final String PROPERTY_USE_COSTUM_PREDICTION = "useCostumPrediction";
   private String APIUsername;
   private double garbageFactor;
   private Boolean showLinear;
   private Boolean showExp;
   private Boolean showTimer;
   protected PropertyChangeSupport listeners;
   private double cumulativeProbabilityScaling;
   private int numBadTrys;
   private Boolean useCostumPrediction;

   public String getAPIUsername()
   {
      return this.APIUsername;
   }

   public Settings setAPIUsername(String value)
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

   public double getGarbageFactor()
   {
      return this.garbageFactor;
   }

   public Settings setGarbageFactor(double value)
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

   public Boolean getShowLinear()
   {
      return this.showLinear;
   }

   public Settings setShowLinear(Boolean value)
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

   public Settings setShowExp(Boolean value)
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

   public Settings setShowTimer(Boolean value)
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

   public double getCumulativeProbabilityScaling()
   {
      return this.cumulativeProbabilityScaling;
   }

   public Settings setCumulativeProbabilityScaling(double value)
   {
      if (value == this.cumulativeProbabilityScaling)
      {
         return this;
      }

      final double oldValue = this.cumulativeProbabilityScaling;
      this.cumulativeProbabilityScaling = value;
      this.firePropertyChange(PROPERTY_CUMULATIVE_PROBABILITY_SCALING, oldValue, value);
      return this;
   }

   public int getNumBadTrys()
   {
      return this.numBadTrys;
   }

   public Settings setNumBadTrys(int value)
   {
      if (value == this.numBadTrys)
      {
         return this;
      }

      final int oldValue = this.numBadTrys;
      this.numBadTrys = value;
      this.firePropertyChange(PROPERTY_NUM_BAD_TRYS, oldValue, value);
      return this;
   }

   public Boolean getUseCostumPrediction()
   {
      return this.useCostumPrediction;
   }

   public Settings setUseCostumPrediction(Boolean value)
   {
      if (Objects.equals(value, this.useCostumPrediction))
      {
         return this;
      }

      final Boolean oldValue = this.useCostumPrediction;
      this.useCostumPrediction = value;
      this.firePropertyChange(PROPERTY_USE_COSTUM_PREDICTION, oldValue, value);
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
      result.append(' ').append(this.getAPIUsername());
      return result.substring(1);
   }
}
