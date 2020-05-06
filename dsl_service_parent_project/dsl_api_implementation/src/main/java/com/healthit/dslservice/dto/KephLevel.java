/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dto;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author duncan
 */
public enum KephLevel {
    LEVEL0(0),
    LEVEL1(1),
    LEVEL2(2),
    LEVEL3(3),
    LEVEL4(4),
    LEVEL5(5),
    LEVEL6(6),
    LEVEL7(7);
   
   private int kephIndex;

   private KephLevel(int kephIndex) { this.kephIndex = kephIndex; }

   public static KephLevel getKephLevel(int kephIndex) {
      for (KephLevel l : KephLevel.values()) {
          if (l.kephIndex == kephIndex) return l;
      }
      throw new IllegalArgumentException("KephLevel not found");
   }
}
