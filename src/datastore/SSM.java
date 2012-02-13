package datastore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import util.DCUtil;
import util.DWin;
import model.DCTriple;
import model.LensAttrib;
import model.PaneAttrib;

/////////////////////////////////////////////////////////////////////////////////
// SSM (System State Manager)
// Stores the system states that may be shared across different modules 
// for example: 
//   - currently focused group object
//   - the global date range 
/////////////////////////////////////////////////////////////////////////////////
public class SSM {
   private static SSM instance;
   
   //////////////////////
   // Default constructor
   //   Just make sure all the variable have some sort of default value
   ////////////////////////////////////////////////////////////////////////////////
   protected SSM() {
      dirty = 0; 
      dirtyGL = 0;
      selectedGroup = new Hashtable<Integer, Integer>();
      startTimeFrame = "19950101";  // yyyyMMdd
      endTimeFrame   = "19951231";  // yyyyMMdd
      startYear = 1995;
      endYear = 1995;
      startMonth = 0;
      endMonth = 0;
      
      
      renderSihoulette = false;
      mouseX = 0;
      mouseY = 0;
      refreshMagicLens = true;
      refreshOITBuffers = true;
      occlusionLevel = 0;
      //magicLensRadius = 120.0f;
      useGuide = false;
      useCircularLabel = false;
      
     
      relatedList = new Vector<Integer>();
   }
   
   
   public static SSM instance() {
      if (instance == null) instance = new SSM();
      return instance;
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Reset data
   // - destroy all lens
   // - reset near far and fov 
   ////////////////////////////////////////////////////////////////////////////////
   public void reset() {
      fov = 30.0f;   
      nearPlane = 1.0f;
      farPlane = 1000.0f;
      
      globalFetchIdx = 0;
      docStartIdx = 0;
      dirtyGL = 1;
      dirty = 1;
      refreshMagicLens = true;
      refreshOITBuffers = true;
      
      
      manufactureAttrib.selected = null;
      makeAttrib.selected = null;
      modelAttrib.selected = null;
      selectedGroup = new Hashtable<Integer, Integer>();
      relatedList.clear();
   }
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Thanks to IEEE 754 ... we need an EPS number ... thanks to Java...this
   // needs to be a double .... sigh...
   ////////////////////////////////////////////////////////////////////////////////
   public static double EPS = 1E-4;
   
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // 3D perspective variables
   ///////////////////////////////////////////////////////////////////////////////// 
   public float fov = 30.0f;
   public float nearPlane = 1.0f;
   public float farPlane  = 1000.0f; 
   
   
   
   public boolean  renderSihoulette;
   public boolean  useLight;
   public float rotateX = 0.0f;
   public float rotateY = 0.0f;
   
//   public float adjustAlphaValue(Integer groupId, float alpha) {
//      if (groupId == null || currentGroup == null || currentGroup.intValue() == groupId.intValue()) return alpha;
//      return alpha*0.75f;
//   }
//   
   // Need to sync this because graphics
   // and logic are running in different threads ... thanks Java !!!
//   public synchronized void setCurrentGroup(Integer group) {
//      currentGroup = group;   
//   }
//   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Indicators for graphic effects
   ///////////////////////////////////////////////////////////////////////////////// 
   
   public int LEN_TEXTURE_WIDTH = 800;   
   public int LEN_TEXTURE_HEIGHT = 800;
   public boolean refreshMagicLens;
   public boolean refreshOITBuffers = true;
   public boolean refreshOITTexture = true;
   public boolean refreshGlowTexture = true;
   
   public short NUM_LENS = 1;   
   
   
   public Vector<LensAttrib> lensList = new Vector<LensAttrib>();
   
   public void clearLens() {
      for (int i=0; i < lensList.size(); i++) {
         lensList.elementAt(i).magicLensSelected = 0;
      }
   }
   
   public int lensSelected() {
      for (int i=0; i < lensList.size(); i++) {
         if (lensList.elementAt(i).magicLensSelected == 1) return 1;    
      }
      return 0;
   }
   
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // GUI Environment
   ///////////////////////////////////////////////////////////////////////////////// 
   public float sparkLineHeight  = 80;
   public float sparkLineWidth   = 170.0f;
   //public float sparkLineWidth   = 120.0f;
   public int   sparkLineSegment = 50; 
   
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // The current focus group/component
   ///////////////////////////////////////////////////////////////////////////////// 
   //public Integer currentGroup;
   //public Integer selectedGroup;
   public Hashtable<Integer, Integer> selectedGroup;
   
   public Integer occlusionLevel;
   
   public Vector<Integer> relatedList; // The list of components that are related to the selected components
   
   public int maxOccurrence = Integer.MAX_VALUE;
   public int minOccurrence = Integer.MIN_VALUE;
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Switches
   ////////////////////////////////////////////////////////////////////////////////
   public boolean useGuide = false;         // Show various debugging artifacts
   public boolean useCircularLabel = false; // Whether to laybel the sparklines in a circular pattern 
   public boolean showLabels = true;        // Whether to show labels at all
   public boolean captureScreen = false;
   public int sortingMethod = 0;            // Controls how the components are sorted (with respect to rendering order)
   public int colouringMethod = 4;
   public int sparklineMode = 1;
   public boolean useAggregate = false;        // Whether the occurrence count should crawl the parts hierarchy
   public boolean useFullTimeLine = true;      // Whether to use the entire timeline for the component chart
   public boolean useDualDepthPeeling = true;  // Whether to use OIT transparency
   public boolean useConstantAlpha = false;    // Whether or not to use OIT constant alpha
   public boolean useGlow = false;
   
   
   public boolean useLocalFocus = false;       // Whether to nor to render based on current selected components 
   
   
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Indicates the global selected time period
   ///////////////////////////////////////////////////////////////////////////////// 
   public String startTimeFrame;
   public String endTimeFrame;
   public int startMonth = 0;  // 0 - 11
   public int endMonth = 0;    // 0 - 11
   public int startYear = 0;
   public int endYear = 0;
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Just for fun
   // Things that may or may not be useful but seems interesting to do (to me)
   ////////////////////////////////////////////////////////////////////////////////
   public boolean colourRampReverseAlpha = false; // Whether to inverse the alpha in the colour scale 
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Panel management
   ////////////////////////////////////////////////////////////////////////////////
   public PaneAttrib manufactureAttrib = new PaneAttrib(300, 50, 200, 200);
   public PaneAttrib makeAttrib = new PaneAttrib(550, 50, 200, 200);
   public PaneAttrib modelAttrib = new PaneAttrib(800, 50, 200, 200);
   public PaneAttrib yearAttrib = new PaneAttrib(1050, 50, 200, 200);
   
   
   /*
   public float manufactureAnchorX = 300;
   public float manufactureAnchorY = 50;
   public float manufactureYOffset = 200;
   public float manufactureHeight = 200;
   public float manufactureTexHeight = 200;
   public boolean manufactureActive = false;
   */
   
   /*
   public float makeAnchorX = 550;
   public float makeAnchorY = 50;
   public float makeYOffset = 200;
   public float makeHeight = 200;
   public float makeTexHeight = 200;
   public boolean makeActive = false;
   */
   
   /*
   public float modelAnchorX = 800;
   public float modelAnchorY = 50;
   public float modelYOffset = 200;
   public float modelHeight = 200;
   public float modelTexHeight = 200;
   public boolean modelActive = false;
   */
   
   public float saveLoadAnchorX = 850;
   public float saveLoadAnchorY = 950;
   public float saveLoadYOffset = 200;
   public float saveLoadHeight = 200;
   public float saveLoadTexHeight = 200;
   public boolean saveLoadActive = false;
   
   
   
   public float scrollWidth = 200;
   public float defaultScrollHeight = 200;
   
   
   //public String selectedManufacture = null;
   //public String selectedMake = null;
   //public String selectedModel = null;
   public String selectedSaveLoad = null;
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Document Management
   ////////////////////////////////////////////////////////////////////////////////
   public int globalFetchIdx = 0;
   public int globalFetchSize = 30;
   public int docMaxSize = 0;
   public int docStartIdx = 0; 
   public boolean docActive = false;
   public int resizePanel = 0;
   
   // All document sizes are measures in pixels
   public float docAnchorX = 400;                 // Bottom left of the document panel
   public float docAnchorY = 400;                 // Bottom left of the document panel
   public float docPadding = 25;                  // Padding area (for dragging)
   public float docWidth = 400;                  
   public float docHeight = 400;
   public float docHeader = 15;     // not currently used
   public float docFooter = 15;     // not currently used
   public float yoffset = docHeight;
   
   public float t1Height = 0; // bad placement, just a proof of concept here
   public float t2Height = 0; // bad placement, just a proof of concept here
   public int t1Start = 0;
   public int t2Start = globalFetchSize;
   public int docAction = 0;
   
   // Check to see if the mouse cursor is in the document header
   public boolean inDocHeader() {
      float mX = mouseX;
      float mY = windowHeight - mouseY;
      if (mX >= docAnchorX + docPadding && mX <= docAnchorX+docWidth-docPadding) {
         if (mY >= docAnchorY+docHeight-docPadding-docHeader && mY <= docAnchorY+docHeight-docPadding) {
            return true;
         }
      }
      return false;
   }
   
   
   // Check to see if the mouse cursor is in the document footer
   public boolean inDocFooter() {
      float mX = mouseX;
      float mY = windowHeight - mouseY;
      if (mX >= docAnchorX + docPadding && mX <= docAnchorX+docWidth-docPadding) {
         if (mY >= docAnchorY+docPadding && mY <= docAnchorY+docPadding+docFooter) {
            return true;
         }
      }
      return false;
   }
   
   // Check to see if the mouse cursor is in the area where
   // the text is drawn
   public boolean inDocContext() {
      if ( ! docActive ) return false;
      float mX = mouseX;
      float mY = windowHeight - mouseY;
      if (DCUtil.between( mX, docAnchorX, docAnchorX+docWidth)) {
         if (DCUtil.between( mY, docAnchorY, docAnchorY+docHeight)) {
            return true;   
         }
      }
         
      /*   
      if (mX >= docAnchorX  && mX <= docAnchorX+docWidth) {
         if (mX >= docAnchorX + docPadding && mX <= docAnchorX+docWidth-docPadding) {
            return true;
         }
      }
      */
      return false;
   }
   
   // Move to the next document (currently will overflow if it get past the maximum)
//   public void nextDoc() {
//      docStartIdx ++;   
//      System.out.println("Doc Index is : " + docStartIdx);
//      if (docStartIdx == 30) {
//         System.out.println("Pagination up");
//         globalFetchIdx += 10;
//         docStartIdx = 20; 
//         dirty = 1;
//      }
//   }
   
   // Move to the previous document
//   public void previousDoc() {
//      if (docStartIdx > 0) {
//         docStartIdx --;
//         System.out.println("Doc Index is : " + docStartIdx);
//         if (docStartIdx  == 10 ) {
//            System.out.println("Pagination down");   
//            globalFetchIdx -= 10;
//            docStartIdx = 20; 
//            
//            if (globalFetchIdx < 0) {
//                globalFetchIdx = 0;   
//                docStartIdx = 10; 
//            }
//            dirty = 1;
//         } 
//      }      
//   }
   
   
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Handle top elements separately
   // These are activated/de-activated with the mouse press and mouse release 
   // respectively
   ////////////////////////////////////////////////////////////////////////////////
   public static int ELEMENT_NONE = 0;
   public static int ELEMENT_LENS = 1;
   public static int ELEMENT_DOCUMENT = 2;
   public static int ELEMENT_MANUFACTURE_SCROLL = 3;
   public static int ELEMENT_MAKE_SCROLL = 4;
   public static int ELEMENT_MODEL_SCROLL = 5;
   public static int ELEMENT_YEAR_SCROLL = 6;
   public static int ELEMENT_SAVELOAD_SCROLL = 7;
   
   public int topElement = ELEMENT_NONE;
   
   
   ////////////////////////////////////////////////////////////////////////////////
   // Returns the current time
   // in YYYY_MM_DD_HH_MM_SS format
   ////////////////////////////////////////////////////////////////////////////////
   public String now() {
      Calendar cal = Calendar.getInstance();
      SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_h_mm_ss");
      String r = formatter.format(cal.getTime());
      return Const.TMP_DIR + "cap_" + r + ".png";
   }
   
   
   
   // Debugging
   public void timeFrameStatistics() {
      int sidx = CacheManager.instance().getDateKey(startTimeFrame);
      int eidx = CacheManager.instance().getDateKey(endTimeFrame);
      
      Hashtable<Integer, Integer> h = CacheManager.instance().getPartOccurrenceFilter(
            sidx, 
            eidx, 
            startMonth, 
            endMonth, 
            manufactureAttrib.selected, 
            makeAttrib.selected, 
            modelAttrib.selected);   
      
      DWin.instance().debug("====================================");
      Enumeration<Integer> e = h.keys();
      while (e.hasMoreElements()) {
         Integer key = e.nextElement();   
         String part = HierarchyTable.instance().partTable.get(key).elementAt(0);
         DWin.instance().debug( part + " " + h.get(key));
      }
      DWin.instance().debug("====================================");
      
   }
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Window Environment
   ///////////////////////////////////////////////////////////////////////////////// 
   public int mouseX;
   public int mouseY;
   public int oldMouseX;
   public int oldMouseY;
   public int windowWidth;
   public int windowHeight;
   
   
   ///////////////////////////////////////////////////////////////////////////////// 
   // Events - just enums, not bitmasks
   // TODO: this uses a single variable to track mouse events ... this is NOT GOOD....
   // fix this when bored !!!
   ///////////////////////////////////////////////////////////////////////////////// 
   
   public static int STATE_NORMAL   = 0x00000000;
   
   public boolean l_mouseClicked = false;
   public boolean r_mouseClicked = false;
   public boolean l_mousePressed = false;
   public boolean r_mousePressed = false;
   
   
   //public int mouseState =  STATE_NORMAL;
   
   
   // Figure out which parts are selected based on a precedence order of :
   // 1 - UI_LAYER
   // 2 - COMPONENT_LAYER
   // Otherwise an action has taken on non interactive element (IE: camera movement)
   public static int UI_LAYER= 1;
   public static int COMPONENT_LAYER= 2;
   public int currentFocusLayer = 0;
   
   //public int currentState;
   public int dirty = 0;
   public int dirtyGL = 0;
   //public int dirtyFilter = 0;
   public int dirtyLoad = 0;
   public int dirtyDateFilter = 0;
   
}