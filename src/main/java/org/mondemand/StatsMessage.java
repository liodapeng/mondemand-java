/*======================================================================*
 * Copyright (c) 2008, Yahoo! Inc. All rights reserved.                 *
 *                                                                      *
 * Licensed under the New BSD License (the "License"); you may not use  *
 * this file except in compliance with the License.  Unless required    *
 * by applicable law or agreed to in writing, software distributed      *
 * under the License is distributed on an "AS IS" BASIS, WITHOUT        *
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.     *
 * See the License for the specific language governing permissions and  *
 * limitations under the License. See accompanying LICENSE file.        *
 *======================================================================*/

package org.mondemand;

import java.io.Serializable;
import org.mondemand.StatType;

public class StatsMessage implements Serializable {
  private String key = null;
  private StatType type = StatType.Unknown;
  private long counter = 0;

  /**
   * @return the key
   */
  public String getKey() {
    return key;
  }

  /**
   * @param key the key to set
   */
  public void setKey(String key) {
    this.key = key;
  }

  /**
   * @return the counter
   */
  public long getCounter() {
    return counter;
  }

  /**
   * @param counter the counter to set
   */
  public void setCounter(long counter) {
    this.counter = counter;
  }

  /**
   * @return the type
   */
  public StatType getType() {
    return type;
  }

  /**
   * @param type set the type of the stat
   */
  public void setType (StatType type) {
    this.type = type;
  }

  public String toString() {
    return type + " : " + key + " : " + counter;
  }
}
