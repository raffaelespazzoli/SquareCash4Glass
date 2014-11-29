package com.raffaele.squarecash4glass.contacts;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "synch_status", id = BaseColumns._ID)
public class SynchStatusDTO extends Model{

  @Column
  private String lastUpdate;
  @Column
  private int pageCompleted;
  
  public boolean equals(Object obj) {
    return EqualsBuilder.reflectionEquals(this, obj);
  }

  public String toString() {
    return ToStringBuilder.reflectionToString(this);
  }

  public String getLastUpdate() {
    return lastUpdate;
  }

  public void setLastUpdate(String lastUpdate) {
    this.lastUpdate = lastUpdate;
  }

  public int getPageCompleted() {
    return pageCompleted;
  }

  public void setPageCompleted(int pageCompleted) {
    this.pageCompleted = pageCompleted;
  }

  public SynchStatusDTO(String lastUpdate, int pageCompleted) {
    super();
    this.lastUpdate = lastUpdate;
    this.pageCompleted = pageCompleted;
  }

  public SynchStatusDTO() {
    super();
  }
  
  
  
}
