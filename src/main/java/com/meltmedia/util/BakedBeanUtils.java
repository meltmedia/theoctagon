package com.meltmedia.util;

import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * BakedBeanUtils is a variant of the jakarta commons BeanUtils class that offers functionality that is specifically
 * helpful in the context of a boiler-style java application but probably isn't very helpful to anyone else.
 */
public class BakedBeanUtils {

  private static final PropertyUtilsBean propertyUtils = new PropertyUtilsBean();

  /**
   * Emulates BeanUtils copyProperties except does not copy...
   * - null sourceBean properties
   * - properties to which JPA relationship annotations apply
   * - properties to which the Jackson JsonIgnore annotation is applied
   *
   * code shamelessly stolen from commons beanutils and baked with love
   *
   * @param sourceBean the bean from which properties shall be copied
   * @param destinationBean the bean to which properties shall be copied
   * @throws IllegalAccessException
   * @throws NoSuchMethodException
   */
  public static void safelyCopyProperties(Object sourceBean, Object destinationBean) throws HalfBakedBeanException {
    try {
      // for each property in the source bean
      for (PropertyDescriptor sourcePropertyDescriptor : propertyUtils.getPropertyDescriptors(sourceBean)) {
        // gather the details regarding the source and destination properties
        String name = sourcePropertyDescriptor.getName();
        Object sourceValue = propertyUtils.getSimpleProperty(sourceBean, name);
        PropertyDescriptor destinationPropertyDescriptor = propertyUtils.getPropertyDescriptor(destinationBean,sourcePropertyDescriptor.getName());
        // if the property is eligible for copying
        if (  propertyUtils.isReadable(sourceBean, name)
            && propertyUtils.isWriteable(destinationBean, name)
            && ! "class".equals(name) // don't copy class property, because you can't
            && sourceValue != null    // don't copy null sourceBean properties
            && ! hasCopyBlockingAnnotation(sourcePropertyDescriptor)
            && ! hasCopyBlockingAnnotation(destinationPropertyDescriptor)) {
          // copy it!
          BeanUtils.copyProperty(destinationBean, name, sourceValue);
        }
      }
    } catch(Exception e) {
      throw new HalfBakedBeanException("Something bad happened while trying to safelyCopyProperties.",e);
    }
  }

  public static <T> T copyIntoNew(Object sourceBean, Class<T> clazz) {
    try {
      T destinationBean = (T) clazz.newInstance();
      propertyUtils.copyProperties(destinationBean, sourceBean);
      return destinationBean;
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  /**
   * This will trim all the strings on the given bean.
   *
   * @param bean The bena to trim.
   * @throws HalfBakedBeanException
   */
  public static void trimStrings(Object bean) throws HalfBakedBeanException {
    try {
      for (PropertyDescriptor propertyDescriptor : propertyUtils.getPropertyDescriptors(bean)) {
        if (propertyDescriptor.getPropertyType() == String.class) {
          BeanUtils.setProperty(bean, propertyDescriptor.getName(), StringUtils.trimToNull(BeanUtils.getProperty(bean, propertyDescriptor.getName())));
        }
      }
    } catch (Exception e) {
      throw new HalfBakedBeanException("Something bad happened while trying to trimStrings.",e);
    }
  }

  public static boolean hasCopyBlockingAnnotation(PropertyDescriptor propertyDescriptor) {
    Method propertyReadMethod = propertyDescriptor.getReadMethod();
    // don't copy from JPA relationship annotated properties
    return propertyReadMethod.isAnnotationPresent(ManyToMany.class) ||
        propertyReadMethod.isAnnotationPresent(ManyToOne.class)  ||
        propertyReadMethod.isAnnotationPresent(OneToMany.class)  ||
        propertyReadMethod.isAnnotationPresent(OneToOne.class)   ||
        // don't copy from Jackson JsonIgnore annotated properties
        propertyReadMethod.isAnnotationPresent(JsonIgnore.class);
  }

  /**
   *  Methods in this class will tend to have large sets of throwable exceptions that we cannot really do anything
   *  about. Rather than throw them all or throw Exception, this exception class should be thrown for BakedBeanUtils
   *  specific, general errors.
   */
  public static class HalfBakedBeanException extends Exception {
    private static final long serialVersionUID = 2661973068962945629L;

    public HalfBakedBeanException(String message, Throwable cause) {
      super(message, cause);
    }
  }

}
