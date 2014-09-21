package com.kanjikyoushi.util;

import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.faces.context.FacesContext;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.log4j.Logger;

public class Resources {

  /**
   * show tables
   * 
   * @param em
   */
  public static void showTableNames(EntityManager em) {

    try {
      Query tableQuery = em
          .createNativeQuery("SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = SCHEMA()");
      List<?> tables = tableQuery.getResultList();
      for (Object tableRow : tables) {
        if (tableRow instanceof Object[]) {
          System.out.println("TABLE ROW");
          for (Object tableColumn : (Object[]) tableRow) {
            if (tableColumn == null) {
              System.out.println("TABLE COLUMN: null");
            } else {
              System.out.println("TABLE COLUMN: ("
                  + tableColumn.getClass().getCanonicalName() + ") "
                  + tableColumn);
            }
          }
        } else {
          System.out.println("TABLE ROW: ("
              + tableRow.getClass().getCanonicalName() + ") " + tableRow);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  @Produces
  @PersistenceContext
  private EntityManager em;

  @Produces
  @RequestScoped
  public FacesContext produceFacesContext() {
    return FacesContext.getCurrentInstance();
  }

  @Produces
  public Logger produceLog(InjectionPoint injectionPoint) {
    return Logger.getLogger(injectionPoint.getMember().getDeclaringClass());
  }

}
