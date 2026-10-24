package com.github.pet.pycharm;

import com.github.pet.pycharm.icons.PetVectorRenderer;
import com.github.pet.pycharm.models.*;
import com.github.pet.pycharm.state.PetPersistentState;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.management.ManagementFactory;
import java.util.*;

/** Standalone benchmark for comparing distribution JARs without build activity. */
public final class RenderBenchmark {
    private static volatile int sink;
    public static void main(String[] args) {
        var state=new PetPersistentState();state.toggleCompanion(PetType.CAT);state.toggleCompanion(PetType.DUCK);
        var image=new BufferedImage(224,24,BufferedImage.TYPE_INT_ARGB);var g=image.createGraphics();g.translate(36,15);g.scale(.81,.81);
        var species=PetType.values();Set<String> accessories=Set.of();
        for(int i=0;i<8000;i++)for(var type:species)PetVectorRenderer.INSTANCE.renderPet(g,type,PetActivityState.IDLE,i,2,true,0,false,accessories,false,0,PetCoat.ORIGINAL,0,0,false);
        for(int i=0;i<100000;i++)sink=state.getVisiblePets().size();
        var bean=(com.sun.management.ThreadMXBean)ManagementFactory.getThreadMXBean();
        long allocated=bean.getCurrentThreadAllocatedBytes();long started=System.nanoTime();
        for(int i=0;i<250000;i++)sink=state.getVisiblePets().size();
        double lookupMs=(System.nanoTime()-started)/1e6;double lookupBytes=(bean.getCurrentThreadAllocatedBytes()-allocated)/250000d;
        long[] times=new long[6000];allocated=bean.getCurrentThreadAllocatedBytes();
        for(int i=0;i<times.length;i++){
            started=System.nanoTime();for(var type:species)PetVectorRenderer.INSTANCE.renderPet(g,type,PetActivityState.IDLE,i,2,true,0,false,accessories,false,0,PetCoat.ORIGINAL,0,0,false);
            times[i]=System.nanoTime()-started;
        }
        double frameBytes=(bean.getCurrentThreadAllocatedBytes()-allocated)/(double)times.length;g.dispose();Arrays.sort(times);
        System.out.printf(Locale.ROOT,"%s,lookup_ms=%.3f,lookup_bytes=%.1f,frame_p50_ms=%.3f,frame_p95_ms=%.3f,frame_bytes=%.0f%n",args[0],lookupMs,lookupBytes,times[times.length/2]/1e6,times[(int)(times.length*.95)]/1e6,frameBytes);
    }
}
