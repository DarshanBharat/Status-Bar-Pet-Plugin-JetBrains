package com.github.pet.pycharm;

import com.github.pet.pycharm.icons.PetParticleManager;
import com.github.pet.pycharm.icons.PetVectorRenderer;
import com.github.pet.pycharm.models.*;
import com.github.pet.pycharm.state.PetPersistentState;
import com.github.pet.pycharm.ui.PetMenuPanel;
import com.github.pet.pycharm.ui.PetStatusBarWidget;
import com.github.pet.pycharm.ui.AnimationSchedule;
import com.github.pet.pycharm.tracking.EditOriginClassifier;
import com.github.pet.pycharm.reporting.PetErrorReportSubmitter;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.lang.reflect.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

/** Dependency-free release regressions; run with Gradle releaseChecks. */
public final class ReleaseChecks {
    interface Checked { void run() throws Exception; }
    static final List<String> lines = new ArrayList<>();
    static int passed, failed;
    static PetPersistentState currentState;
    static final List<Runnable> dispatchQueue = new ArrayList<>();
    static void check(boolean value, String message) { if (!value) throw new AssertionError(message); }
    static void test(String name, Checked action) {
        try { action.run(); passed++; lines.add("PASS " + name); }
        catch (Throwable e) { failed++; lines.add("FAIL " + name + ": " + (e instanceof InvocationTargetException ? e.getCause() : e)); }
        System.out.println(lines.getLast());
    }
    static Field field(Class<?> type, String name) throws Exception { var f=type.getDeclaredField(name);f.setAccessible(true);return f; }
    static Object get(Object target,String name)throws Exception{return field(target.getClass(),name).get(target);}
    static void set(Object target,String name,Object value)throws Exception{field(target.getClass(),name).set(target,value);}
    static Method method(Class<?> type,String name,Class<?>...args)throws Exception{var m=type.getDeclaredMethod(name,args);m.setAccessible(true);return m;}
    static Object defaults(Object proxy,Method method,Object[] args) {
        return switch(method.getName()) {
            case "hashCode" -> System.identityHashCode(proxy);
            case "equals" -> proxy == args[0];
            case "toString" -> "Release check stub";
            case "getService" -> args[0] == PetPersistentState.class ? currentState : null;
            case "invokeLater" -> { dispatchQueue.add((Runnable)args[0]); yield null; }
            case "isUnitTestMode", "isHeadlessEnvironment", "isDispatchThread", "isReadAccessAllowed", "isWriteAccessAllowed" -> true;
            default -> method.getReturnType() == boolean.class ? false : method.getReturnType() == int.class ? 0 : method.getReturnType() == long.class ? 0L : null;
        };
    }
    static Project project(){return (Project)Proxy.newProxyInstance(ReleaseChecks.class.getClassLoader(),new Class<?>[]{Project.class},ReleaseChecks::defaults);}
    static void layout(Component component){if(component instanceof Container c){c.doLayout();for(var child:c.getComponents())layout(child);}}
    static List<Component> descendants(Component c){var list=new ArrayList<Component>();list.add(c);if(c instanceof JMenu m){for(var child:m.getMenuComponents())list.addAll(descendants(child));}else if(c instanceof Container p){for(var child:p.getComponents())list.addAll(descendants(child));}return list;}
    static void widgetChecks()throws Exception {
        currentState=new PetPersistentState();
        var app=(Application)Proxy.newProxyInstance(ReleaseChecks.class.getClassLoader(),new Class<?>[]{Application.class},ReleaseChecks::defaults);
        // The stub exists only inside this test JVM, never in the user's IDE.
        ApplicationManager.setApplication(app);
        var project=project();
        var widget=new PetStatusBarWidget(project);
        try {
            var component=widget.getComponent();
            test("Width caps and narrow layouts",()->{
                for(String width:List.of("compact","normal","wide"))for(int count=1;count<=3;count++){
                    currentState.setWidthId(width);currentState.getCompanionPetIds().clear();
                    if(count>=2)currentState.getCompanionPetIds().add("cat");if(count==3)currentState.getCompanionPetIds().add("duck");
                    method(widget.getClass(),"updateSize").invoke(widget);
                    int base=width.equals("compact")?72:width.equals("wide")?180:112;
                    check(component.getPreferredSize().width==base*(count+1)/2,"Width multiplier");
                    for(int h:new int[]{18,24,32})for(int w:new int[]{component.getMinimumSize().width,component.getPreferredSize().width}){
                        component.setSize(w,h);
                        float scale=(float)method(widget.getClass(),"getScale").invoke(widget);
                        check(Float.isFinite(scale)&&scale>0,"Finite positive scale");
                        var image=new BufferedImage(w,h,BufferedImage.TYPE_INT_ARGB);var g=image.createGraphics();component.paint(g);g.dispose();
                        var centers=new ArrayList<Float>();
                        for(var pet:currentState.getVisiblePets()){
                            var motion=((Map<?,?>)get(widget,"motions")).get(pet.getId());float x=(float)get(motion,"x");
                            check(x>=0&&x<=w,"Awake pet left the widget");
                            for(float prior:centers)check(Math.abs(prior-x)+.01f>=33f*scale,"Companions overlap: mode="+width+", count="+count+", width="+w+", height="+h+", positions="+centers+" and "+x+", scale="+scale);centers.add(x);
                        }
                    }
                }
            });
            test("Sleep and wake stay horizontally fixed",()->{
                currentState.getCompanionPetIds().clear();currentState.setReducedMotion(false);component.setSize(112,24);
                var motion=method(widget.getClass(),"motionFor",PetType.class,int.class,int.class).invoke(widget,PetType.DOG,0,1);
                var rest=method(widget.getClass(),"advanceRest",motion.getClass(),PetActivityState.class,long.class,float.class);
                float x=(float)get(motion,"x");long start=System.currentTimeMillis();
                for(int i=0;i<45;i++){rest.invoke(widget,motion,PetActivityState.SLEEPING,start+i*55L,55f);check((float)get(motion,"x")==x,"Sleep position drift");}
                check((float)get(motion,"sleepBlend")==1f,"Sleeping transition completes");
                for(int i=0;i<40;i++){rest.invoke(widget,motion,PetActivityState.IDLE,start+3000+i*55L,55f);check((float)get(motion,"x")==x,"Wake position drift");}
                check((float)get(motion,"sleepBlend")==0f&&!(boolean)get(motion,"sleeping"),"Wake transition completes");
            });
            test("Reduced motion rests immediately",()->{
                currentState.setReducedMotion(true);
                var motion=method(widget.getClass(),"motionFor",PetType.class,int.class,int.class).invoke(widget,PetType.DOG,0,1);
                method(widget.getClass(),"advanceRest",motion.getClass(),PetActivityState.class,long.class,float.class).invoke(widget,motion,PetActivityState.SLEEPING,System.currentTimeMillis(),55f);
                check((float)get(motion,"sleepBlend")==1f,"Static sleep pose");
            });
            test("Hidden widget stops animation",()->{
                method(widget.getClass(),"updateAnimationSchedule").invoke(widget);
                check(!((Timer)get(widget,"animationTimer")).isRunning(),"Hidden animation still running");
            });
            test("Hidden widget does not construct popups",()->method(widget.getClass(),"message",String.class).invoke(widget,"Hidden-window completion"));
            test("Typing during freeze still refreshes inactivity",()->{
                currentState.setPaused(true);set(widget,"lastActivity",System.currentTimeMillis()-60000);
                try {
                    method(widget.getClass(),"onTyping").invoke(widget);
                    check(System.currentTimeMillis()-(long)get(widget,"lastActivity")<1000,"Frozen pet forgot ongoing editing");
                } finally {currentState.setPaused(false);}
            });
            test("Burst notifications coalesce without losing edit counters",()->{
                dispatchQueue.clear();long edits=currentState.getManualEditCount();
                for(int i=0;i<10000;i++){currentState.recordManualEdit(1,0);PetStatusBarWidget.Companion.notifyLiveTyping(1,project);}
                check(dispatchQueue.size()==1,"Thousands of UI callbacks were queued");
                check(currentState.getManualEditCount()-edits==10000,"Coalescing lost statistics");
                var queued=new ArrayList<>(dispatchQueue);dispatchQueue.clear();queued.forEach(Runnable::run);
            });
            test("Shared settings resize another window",()->{
                currentState.setWidthId("compact");currentState.getCompanionPetIds().add("cat");currentState.getCompanionPetIds().add("duck");
                method(widget.getClass(),"heartbeat").invoke(widget);
                check(component.getPreferredSize().width==144,"Remote window kept stale width");
            });
        } finally {
            ((Timer)get(widget,"messageTimer")).start();
            ((Timer)get(widget,"animationTimer")).start();
            widget.dispose();
            test("Dispose stops all widget timers",()->{
                check(!((Timer)get(widget,"animationTimer")).isRunning(),"Animation timer leak");
                check(!((Timer)get(widget,"clockTimer")).isRunning(),"Clock timer leak");
                check(!((Timer)get(widget,"messageTimer")).isRunning(),"Message timer leak");
            });
            ApplicationManager.setApplication(null);
        }
    }
    static void stateChecks() {
        test("Bulk typing XP and AI isolation",()->{
            var s=new PetPersistentState();s.recordManualEdit(12,0);s.recordManualEdit(3,1);s.recordAiAgentEdit(10000,400,"Codex");
            check(s.getXp()==3&&s.getTotalKeystrokes()==15&&s.getAiAgentEditCount()==1,"XP provenance");
        });
        test("Idle heartbeat cannot keep a streak",()->{
            var s=new PetPersistentState();String prior=LocalDate.now().minusDays(2).toString();s.setLastActiveDate(prior);s.setStreakDays(7);s.recordCodingTimeTick(1);
            check(s.getLastActiveDate().equals(prior)&&s.getStreakDays()==7,"Idle heartbeat changed active date");
        });
        test("Partial active minutes survive a break",()->{
            var s=new PetPersistentState();set(s,"activeCodingSecondsInInterval",59);set(s,"lastKeystrokeTimestamp",System.currentTimeMillis()-60000);set(s,"lastCodingTickTimestamp",System.currentTimeMillis()-1000);
            s.recordCodingTimeTick(1);check((int)get(s,"activeCodingSecondsInInterval")==59,"Partial coding time discarded");
        });
        test("New editing session resets flow bonus",()->{
            var s=new PetPersistentState();set(s,"continuousFlowMinutes",29);set(s,"lastKeystrokeTimestamp",System.currentTimeMillis()-60000);
            s.recordManualEdit(1,0);check((int)get(s,"continuousFlowMinutes")==0,"Flow carried over idle break");
        });
        test("Daily counters roll over only on activity",()->{
            var s=new PetPersistentState();s.setLastActiveDate(LocalDate.now().minusDays(1).toString());s.setKeystrokesToday(100);s.setCodingMinutesToday(99);s.recordManualEdit(1,0);
            check(s.getKeystrokesToday()==1&&s.getCodingMinutesToday()==0&&s.getStreakDays()==2,"Daily rollover");
        });
        test("Invalid or future active dates reset safely",()->{
            for(String date:List.of("bad-date",LocalDate.now().plusDays(1).toString())){
                var s=new PetPersistentState();s.setLastActiveDate(date);s.setKeystrokesToday(99);s.setStreakDays(40);s.recordManualEdit(1,0);
                check(s.getStreakDays()==1&&s.getKeystrokesToday()==1,"Bad date retained stale counters");
            }
        });
        test("XP addition cannot wrap negative",()->{var s=new PetPersistentState();s.setXp(99);s.addXp(Long.MAX_VALUE);check(s.getXp()>=0&&s.getLevel()>0,"XP overflow");});
        test("XP curve agrees with ordinary sequential leveling",()->{
            var random=new Random(41);for(int round=0;round<1000;round++){
                int level=1+random.nextInt(100);long xp=random.nextInt(10000),amount=random.nextInt(100000);int expected=level;long remainder=xp+amount;
                for(;;){long required=75+25L*expected+5L*(expected-1)*(expected-1);if(remainder<required)break;remainder-=required;expected++;}
                var s=new PetPersistentState();s.setLevel(level);s.setXp(xp);s.addXp(amount);
                check(s.getLevel()==expected&&s.getXp()==remainder,"XP curve changed at "+level);
            }
        });
        test("Active time is counted once across windows",()->{
            var s=new PetPersistentState();set(s,"lastKeystrokeTimestamp",System.currentTimeMillis());set(s,"lastCodingTickTimestamp",System.currentTimeMillis()-2100);
            s.recordCodingTimeTick(1);int seconds=(int)get(s,"activeCodingSecondsInInterval");s.recordCodingTimeTick(1);
            check(seconds==2&&(int)get(s,"activeCodingSecondsInInterval")==2,"Multiple windows doubled time");
        });
        test("Stat counters saturate safely",()->{
            var s=new PetPersistentState();s.setTotalKeystrokes(Long.MAX_VALUE-1);s.recordManualEdit(100,0);s.setAiAgentCharsAdded(Long.MAX_VALUE-1);s.recordAiAgentEdit(100,0,"Codex");
            check(s.getTotalKeystrokes()==Long.MAX_VALUE&&s.getAiAgentCharsAdded()==Long.MAX_VALUE,"Counter wrapped negative");
        });
        test("Saved state is detached from live mutable collections",()->{
            var s=new PetPersistentState();s.recordAiAgentEdit(20,0,"Codex");var saved=s.getState();s.getCustomNames().put("dog","Changed");s.recordAiAgentEdit(20,0,"Codex");
            check(!saved.getPetName().equals("Changed")&&saved.getAiAgentEditCount()==1&&saved.getAiAgentEditsByAgent().get("Codex")==1,"Snapshot changed after saving");
        });
        test("Feeding recovers from a backwards clock",()->{var s=new PetPersistentState();s.setLastFedTimestamp(System.currentTimeMillis()+86400000);check(s.feed().getSecond()==15L,"Future timestamp blocks feeding");});
        test("Companion switches remain unique and stable",()->{
            var s=new PetPersistentState();s.toggleCompanion(PetType.CAT);s.toggleCompanion(PetType.DUCK);var before=s.getVisiblePets();s.setSelectedPetType(PetType.CAT);
            check(s.getVisiblePets().equals(before)&&s.getVisiblePets().size()==3,"Companion reorder");
        });
        test("Selection cap: at most 3 pets can be selected",()->{
            var s=new PetPersistentState();
            s.setSelectedPetType(PetType.DOG);
            s.toggleCompanion(PetType.CAT);
            s.toggleCompanion(PetType.DUCK);
            check(s.getVisiblePets().size()==3,"3 pets selected");
            s.toggleCompanion(PetType.CRAB);
            check(s.getVisiblePets().size()==3,"Selection exceeded 3 pets");
            check(!s.getCompanionPetIds().contains("crab"),"Crab was added when cap of 3 was reached");
            s.toggleCompanion(PetType.CAT);
            check(s.getVisiblePets().size()==2,"Companion deselected");
            s.toggleCompanion(PetType.CRAB);
            check(s.getVisiblePets().size()==3,"Crab companion added up to 3 pets");
            check(s.getCompanionPetIds().contains("crab"),"Crab is present");
        });
        test("Break reminders are optional and persist in state",()->{
            var s=new PetPersistentState();
            check(!s.getHealthRemindersEnabled(),"Health reminders should be off by default");
            s.setHealthRemindersEnabled(true);
            var copy=s.getState();
            check(copy.getHealthRemindersEnabled(),"Health reminders enabled state did not copy");
        });
        test("Motion menu contains all movement, speed, width and preference options",()->{
            var s=new PetPersistentState();Function0<Unit> no=()->Unit.INSTANCE;
            var root=new PetMenuPanel(s,()->false,no,no,no,no,no);
            var motion=(JMenu)Arrays.stream(root.getComponents()).filter(c->c instanceof JMenu m&&m.getText().equals("Motion")).findFirst().orElseThrow();
            var items=Arrays.stream(motion.getPopupMenu().getComponents()).filter(c->c instanceof AbstractButton).map(c->((AbstractButton)c).getText()).toList();
            check(items.containsAll(List.of("Wander around","Follow mouse","Companion interactions","Slow","Calm","Lively","Compact","Comfortable","Wide","Break & stretch reminders","Freeze animation")),"Missing motion choices: "+items);
            check(!items.contains("Reduced motion"),"Reduced motion should be removed from menu");
        });
        test("Hat replacement retains only one hat",()->{var s=new PetPersistentState();s.setLevel(20);s.toggleAccessory(PetAccessory.CROWN);s.toggleAccessory(PetAccessory.WIZARD_HAT);check(!s.getActiveAccessories().contains("crown")&&s.getActiveAccessories().contains("hat"),"Hat conflict");});
        test("Preference changes wait for an in-progress save",()->{
            var s=new PetPersistentState();
            var started=new java.util.concurrent.CountDownLatch(1);
            var worker=new Thread(()->{started.countDown();s.toggleCompanion(PetType.CAT);});
            boolean blocked;
            synchronized(s){
                worker.start();check(started.await(2,java.util.concurrent.TimeUnit.SECONDS),"Worker did not start");
                long deadline=System.nanoTime()+java.util.concurrent.TimeUnit.SECONDS.toNanos(2);
                while(worker.isAlive()&&worker.getState()!=Thread.State.BLOCKED&&System.nanoTime()<deadline)Thread.yield();
                blocked=worker.getState()==Thread.State.BLOCKED;
            }
            worker.join(2000);check(!worker.isAlive(),"Preference change did not finish");
            check(blocked&&s.getCompanionPetIds().contains("cat"),"Preference mutation raced with state saving");
        });
    }
    static void uiChecks()throws Exception {
        UIManager.put("Label.font",new Font("Segoe UI",Font.PLAIN,13));
        test("Animation scheduling handles focus, minimize, pause and reduced motion",()->{
            var policy=AnimationSchedule.INSTANCE;
            check(policy.delayMs(false,false,false,true,false,33)==-1,"Hidden window animates");
            check(policy.delayMs(true,true,false,true,false,33)==-1,"Minimized window animates");
            check(policy.delayMs(true,false,true,true,false,33)==-1,"Paused pet animates");
            check(policy.delayMs(true,false,false,false,false,33)==300,"Background throttle");
            check(policy.delayMs(true,false,false,true,true,33)==160,"Reduced motion throttle");
            check(policy.delayMs(true,false,false,true,false,33)==33,"Foreground speed");
        });
        test("Compact menu contains no removed options",()->{
            Function0<Unit> no=()->Unit.INSTANCE;var root=new PetMenuPanel(new PetPersistentState(),()->false,no,no,no,no,no);
            check(root.getPreferredSize().height<=280,"Main menu too tall");
            for(var c:descendants(root))if(c instanceof AbstractButton b)check(!b.getText().matches(".*(Achievements|achievements|Report an issue|Coat color|Reduced motion).*"),"Removed menu option: "+b.getText());
        });
        test("Menu choices apply without dismissing the menu",()->{
            var s=new PetPersistentState();Function0<Unit> no=()->Unit.INSTANCE;var root=new PetMenuPanel(s,()->false,no,no,no,no,no);
            var cat=(JMenuItem)descendants(root).stream().filter(c->c instanceof JRadioButtonMenuItem b&&b.getText().equals("Luna")).findFirst().orElseThrow();
            var manager=new MenuSelectionManager(){@Override public void clearSelectedPath(){throw new AssertionError("Menu was dismissed");}};
            method(cat.getUI().getClass(),"doClick",MenuSelectionManager.class).invoke(cat.getUI(),manager);
            check(s.getSelectedPetType()==PetType.CAT,"Pet selection didn't update");
            var wander=(JMenuItem)descendants(root).stream().filter(c->c instanceof JCheckBoxMenuItem b&&b.getText().equals("Wander around")).findFirst().orElseThrow();
            method(wander.getUI().getClass(),"doClick",MenuSelectionManager.class).invoke(wander.getUI(),manager);check(!s.getWanderingEnabled(),"Wander around toggle didn't update");
        });
        test("Long names reserve room for complete XP",()->{
            var s=new PetPersistentState();s.getCustomNames().put("dog","WWWWWWWWWWWWWWWWWWWW");s.setLevel(100);s.setXp(43000);
            Function0<Unit> no=()->Unit.INSTANCE;var root=new PetMenuPanel(s,()->false,no,no,no,no,no);root.setSize(root.getPreferredSize());layout(root);
            var xp=(JLabel)get(root,"progressLabel");check(xp.getWidth()>=xp.getPreferredSize().width,"XP label clipped by long name");
        });
        test("User names are always plain text",()->{
            var s=new PetPersistentState();s.getCustomNames().put("dog","<html><b>name</b>");Function0<Unit> no=()->Unit.INSTANCE;
            var root=new PetMenuPanel(s,()->false,no,no,no,no,no);
            var name=(JLabel)get(root,"nameLabel");check(name.getClientProperty("html")==null,"User name interpreted as HTML");
        });
        test("All species and activity states render at fractional scales",()->{
            for(var type:PetType.values())for(var state:PetActivityState.values())for(double scale:new double[]{.6,1,1.25,1.5,2}){
                var pic=new BufferedImage(96,96,BufferedImage.TYPE_INT_ARGB);var g=pic.createGraphics();g.translate(48,48);g.scale(scale,scale);
                var transform=g.getTransform();var composite=g.getComposite();
                PetVectorRenderer.INSTANCE.renderPet(g,type,state,24,2,true,1f,false,Set.of("glasses","crown"),true,.5f,PetCoat.ORIGINAL,state==PetActivityState.SLEEPING?1f:0f,.5f,false);
                check(g.getTransform().equals(transform)&&g.getComposite().equals(composite),"Renderer leaked graphics state");g.dispose();
            }
        });
        test("Particles expire without concurrent modifications",()->{
            var p=new PetParticleManager();for(int i=0;i<1000;i++)p.spawnHeart(10,10);for(int i=0;i<100;i++)p.update();
            check(((Collection<?>)get(p,"particles")).isEmpty(),"Particles retained after expiry");
        });
        test("Particle bursts are bounded",()->{
            var p=new PetParticleManager();for(int i=0;i<10000;i++)p.spawnHeart(10,10);check(((Collection<?>)get(p,"particles")).size()<=64,"Particle memory is unbounded");
        });
        test("Refactoring shortcuts and undo do not earn typing XP",()->{
            for(String command:List.of("Reformat Code","Rename","Optimize imports","Undo Typing","Redo","Replace All"))check(!EditOriginClassifier.INSTANCE.isManual(command,true),"Programmatic edit treated as typing: "+command);
            for(String command:List.of("Typing","Paste","Backspace","Delete line"))check(EditOriginClassifier.INSTANCE.isManual(command,false),"Manual command rejected");
            check("Codex".equals(EditOriginClassifier.INSTANCE.namedAgent("Apply Codex changes")),"Named AI detection");
        });
        test("Localized typing still counts without classifying formatter shortcuts as typing",()->{
            var source=new JPanel();long now=System.currentTimeMillis();
            var typed=new java.awt.event.KeyEvent(source,java.awt.event.KeyEvent.KEY_TYPED,now,0,java.awt.event.KeyEvent.VK_UNDEFINED,'文');
            check(EditOriginClassifier.INSTANCE.isDirectTyping(typed),"Localized typed character lost");
            var format=new java.awt.event.KeyEvent(source,java.awt.event.KeyEvent.KEY_PRESSED,now,java.awt.event.InputEvent.CTRL_DOWN_MASK|java.awt.event.InputEvent.ALT_DOWN_MASK,java.awt.event.KeyEvent.VK_L,'L');
            check(!EditOriginClassifier.INSTANCE.isDirectTyping(format),"Formatter shortcut counted as typing");
        });
        test("Localized Enter and Tab are manual edits",()->{
            for(int key: new int[]{java.awt.event.KeyEvent.VK_ENTER,java.awt.event.KeyEvent.VK_TAB}){
                var event=new java.awt.event.KeyEvent(new JPanel(),java.awt.event.KeyEvent.KEY_PRESSED,System.currentTimeMillis(),0,key,java.awt.event.KeyEvent.CHAR_UNDEFINED);
                check(EditOriginClassifier.INSTANCE.isDirectTyping(event),"Localized control key was missed");
            }
            check(EditOriginClassifier.INSTANCE.isManual("Localized typing command",true),"Direct typing ignored for localized command");
            check(!EditOriginClassifier.INSTANCE.isManual("",false),"Unknown non-typing edit counted as personal");
        });
        test("Email draft is bounded and Unicode-safe for large reports",()->{
            var companion=PetErrorReportSubmitter.Companion;
            var draft=method(companion.getClass(),"emailDraftUrl",String.class);
            for(String text:List.of("a".repeat(1000000),"😀 中文 & ? #".repeat(50000))){
                String url=(String)draft.invoke(companion,text);check(url.length()<=1800&&url.startsWith("mailto:contact@darshan-bharat.com?"),"Malformed draft URL");
                String body=java.net.URLDecoder.decode(url.substring(url.indexOf("&body=")+6),java.nio.charset.StandardCharsets.UTF_8);check(!body.contains("\uFFFD"),"Split Unicode code point");
            }
        });
    }
    static void performance()throws Exception {
        var s=new PetPersistentState();s.toggleCompanion(PetType.CAT);s.toggleCompanion(PetType.DUCK);
        var bean=(com.sun.management.ThreadMXBean)java.lang.management.ManagementFactory.getThreadMXBean();
        for(int i=0;i<50000;i++)s.getVisiblePets();
        long allocated=bean.getCurrentThreadAllocatedBytes();long started=System.nanoTime();int total=0;
        for(int i=0;i<250000;i++)total+=s.getVisiblePets().size();
        long elapsed=System.nanoTime()-started;allocated=bean.getCurrentThreadAllocatedBytes()-allocated;
        lines.add(String.format(Locale.ROOT,"PERF visible-pet lookups: %.3f ms / 250,000 calls, %.1f bytes/call; checksum=%d",elapsed/1e6,allocated/250000d,total));
        var image=new BufferedImage(224,24,BufferedImage.TYPE_INT_ARGB);var g=image.createGraphics();g.translate(36,15);g.scale(.81,.81);
        for(int i=0;i<500;i++)for(var type:PetType.values())PetVectorRenderer.INSTANCE.renderPet(g,type,PetActivityState.IDLE,i,2,true,0,false,Set.of(),false,0,PetCoat.ORIGINAL,0,0,false);
        var times=new long[3000];allocated=bean.getCurrentThreadAllocatedBytes();
        for(int i=0;i<times.length;i++){started=System.nanoTime();for(var type:PetType.values())PetVectorRenderer.INSTANCE.renderPet(g,type,PetActivityState.IDLE,i,2,true,0,false,Set.of(),false,0,PetCoat.ORIGINAL,0,0,false);times[i]=System.nanoTime()-started;}
        allocated=bean.getCurrentThreadAllocatedBytes()-allocated;g.dispose();Arrays.sort(times);
        lines.add(String.format(Locale.ROOT,"PERF three-pet offscreen rendering: p50 %.3f ms, p95 %.3f ms, %.0f bytes/frame",times[1500]/1e6,times[2850]/1e6,allocated/3000d));
        lines.stream().filter(l->l.startsWith("PERF")).forEach(System.out::println);
    }
    public static void main(String[]args)throws Exception {
        SwingUtilities.invokeAndWait(()->{stateChecks();try{uiChecks();widgetChecks();performance();}catch(Exception e){failed++;lines.add("FAIL harness: "+e);e.printStackTrace();}});
        String summary="Passed: "+passed+"; failed: "+failed;lines.add(summary);System.out.println(summary);
        Path report=Path.of(System.getProperty("pet.checkReport","build/reports/release-checks.txt"));Files.createDirectories(report.getParent());Files.write(report,lines);
        if(failed>0)throw new AssertionError(summary);
    }
}
