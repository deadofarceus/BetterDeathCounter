package betterdeathcounter.model;
import java.beans.PropertyChangeSupport;

public class Death
{
   public static final String PROPERTY_PERCENTAGE = "percentage";
   public static final String PROPERTY_TIME = "time";
   private int percentage;
   private int time;
   protected PropertyChangeSupport listeners;

   public int getPercentage()
   {
      return this.percentage;
   }

   public Death setPercentage(int value)
   {
      if (value == this.percentage)
      {
         return this;
      }

      final int oldValue = this.percentage;
      this.percentage = value;
      this.firePropertyChange(PROPERTY_PERCENTAGE, oldValue, value);
      return this;
   }

   public int getTime()
   {
      return this.time;
   }

   public Death setTime(int value)
   {
      if (value == this.time)
      {
         return this;
      }

      final int oldValue = this.time;
      this.time = value;
      this.firePropertyChange(PROPERTY_TIME, oldValue, value);
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
}
