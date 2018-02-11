package assignment1



import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import java.util.concurrent.TimeUnit

//http://stackoverflow.com/questions/4359869/sorting-2-dimensional-java-array
class Compare2DArray implements Comparator {
    public int compare(Object a, Object b) {
        int[] aa = (int[]) a;
        int[] bb = (int[]) b;
        for (int i = 0; i < aa.length && i < bb.length; i++)
            if (aa[i] != bb[i])
                return aa[i] - bb[i];
        return aa.length - bb.length;
    }
}
//my own implementation sort by x
class Compare2DArrayX implements Comparator {
    public int compare(Object a, Object b) {
        int[] aa = (int[]) a;
        int[] bb = (int[]) b;
        if (aa[0] != bb[0])
            return aa[0] - bb[0];
        return aa.length - bb.length;
    }
}

//my own implementation sort by y
class Compare2DArrayY implements Comparator {
    public int compare(Object a, Object b) {
        int[] aa = (int[]) a;
        int[] bb = (int[]) b;
        if (aa[1] != bb[1])
            return aa[1] - bb[1];
        return aa.length - bb.length;
    }
}

@Transactional(readOnly = true)
class PointController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    //How many points are going to be generated using this base
    def index4Points = 5000;
    //If declared, it will restrict the limit of points
    def pointRestriction = 1000;
    def t1 = 0;
    def t2 = 0;
    def t3 = 0;
    def t4 = 0;

    def index(Integer max) {
        def pointsList = generatePointsRandomly();
        
        //do a pre-sort
        def mListXY = [];
        pointsList.each{
            mListXY.add([it.x, it.y]);
        }
        pointsList = null;
        def mListX = mListXY.clone();
        def mListY = mListXY.clone();

        //sort x and y
        Collections.sort(mListXY, new Compare2DArray());
        //sort by x axis
        Collections.sort(mListX, new Compare2DArrayX());
        //sort by y axis
        Collections.sort(mListY, new Compare2DArrayY());

        def closestPoints = findClosestPoints(mListXY, mListX, mListY, true);
        //print "List: " + mListXY;
        print "Total of points: " + mListXY.size();
        //print "The closest points are: " + closestPoints;
        print "Total time spent:                  " + ((t2 - t1) + (t4 - t3));

    }

    //receives arrays: original, sorted by x, sorted by y, boolean to measure time if true
    def findClosestPoints(p,x,y,time){
        if(p.size() <= 3)
            return bruteForceClosestPoints(p);
        else{
            time ? t1 = System.currentTimeMillis() : null;
            def sizeLL = Math.ceil(p.size()/2);
            def sizeLR = Math.floor(p.size()/2);
            def l = Math.ceil(p.size()/2);

            def pL = p.subList(0,(int)l);
            def pR = p.subList((int)l,p.size());

            def xL = [];
            def xR = [];
            def yL = [];
            def yR = [];
            def delta, deltaL, deltaR, deltaPoints, deltaLPoints, deltaRPoints;
            def closestPointsL, closestPointsR;

            y.each{
                if(pL.contains(it))
                    yL.add(it);
                else
                    yR.add(it);
            }
            x.each{
                if(pL.contains(it))
                    xL.add(it);
                else
                    xR.add(it);
            }
            time ? t2 = System.currentTimeMillis() : null;
            deltaLPoints = findClosestPoints(pL,xL,yL,false);
            deltaRPoints = findClosestPoints(pR,xR,yR,false);            
            time ? t3 = System.currentTimeMillis() : null;
            deltaL = getDistance(deltaLPoints.getAt(0),deltaLPoints.getAt(1));
            deltaR = getDistance(deltaRPoints.getAt(0),deltaRPoints.getAt(1));
            delta = Math.min(deltaL,deltaR);
            if(delta == deltaL)
                deltaPoints = deltaLPoints;
            else
                deltaPoints = deltaRPoints;
            def yprime = [];
            def median = x[(int)l][0];
            def leftDelta = (median - delta);
            def rightDelta = (median + delta);
            y.each{
                if(it[0] > leftDelta && it[0] < rightDelta)
                    yprime.add(it);
            }
            yprime.eachWithIndex{it, index ->
                for(def i = 1; i < 8; i++){
                    if((index + i) >= yprime.size()){
                        break;
                    }
                    def deltaYPrime = getDistance(it, yprime[index + i]);
                    if(deltaYPrime < delta){
                        delta = deltaYPrime;
                        deltaPoints.clear();
                        deltaPoints.add(it);
                        deltaPoints.add(yprime[index + i]);
                    }
                }
            }
            time ? t4 = System.currentTimeMillis() : null;

            return deltaPoints
        }
            // def t1 = System.currentTimeMillis();
            // print "Total time: " + (t1 - t0);
            // print "Total size of array: " + pointsList.size();
        
    }

    def bruteForceClosestPoints(pointsList){
        def c = 0.0;
        def smallestDistance = 0.0;
        def closestPointsList = [];
        def pointA, pointB;
        if(pointsList.size() == 2){
            closestPointsList.add(pointsList.get(0));
            closestPointsList.add(pointsList.get(1));
            return closestPointsList;
        }else{
            pointsList.each{ pA ->
                pointsList.each{ pB ->
                    if(pA != pB){
                        c = getDistance(pA, pB);
                        if( c < smallestDistance || smallestDistance == 0){
                            smallestDistance = c;
                            pointA = pA;
                            pointB = pB;
                        } 
                    }
                }
            }
        }
        closestPointsList.add(pointA);
        closestPointsList.add(pointB);

        return closestPointsList;
    }

    def getDistance(pA, pB){
        def a = pA[0].abs() - pB[0].abs();
        def b = pA[1].abs() - pB[1].abs();
        def c = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
        //print "Distance from " + pA + " to " + pB + " is = " + c;
        return c;
    }

    def getHRtime(milliseconds){
        return String.format("%d min, %d sec", 
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) - 
                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds))
            );
    }

    def generatePointsRandomly(){
        def pointsList = [];
        Random random = new Random();
        (index4Points).times{
            def newPoint = new Point(random.nextInt(pointRestriction),random.nextInt(pointRestriction));
            pointsList.add(newPoint);
        }
        return pointsList;
    }
}
