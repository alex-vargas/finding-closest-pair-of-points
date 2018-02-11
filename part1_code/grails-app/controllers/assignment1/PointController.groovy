package assignment1



import static org.springframework.http.HttpStatus.*
import grails.transaction.Transactional
import java.util.concurrent.TimeUnit

@Transactional(readOnly = true)
class PointController {

    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    //How many points are going to be generated using this base: squareIndex * squareIndex
    def squareIndex = 20;
    //If declared, it will restrict the limit of points
    def pointRestriction = 10;

    def index(Integer max) {
        def pointsList = generatePointsRandomly();
        printPoints(pointsList);

        def t0 = System.currentTimeMillis();
        findClosestPoints(pointsList);
        def t1 = System.currentTimeMillis();
        print "Total time: " + getHRtime(t1 - t0);
        //Algorithm
        //generate randomly n-points, it has to be on a square range
        //display how many points were generated
        //startClock = identify when this is starting
        //identify the closets points
            // c = 0;
            // smallestDistance = 0;
            // pointA;
            // pointB;
            //for pointsList.each, pA
                //for pointsList.each, pB
                    // if (pA != pB)
                        // c = square root of a square plus b square
                        // if (c < smallestDistance || smallestDistance == 0)
                            // smallestDistance = c
                            // pointA = pA;
                            // pointB = pB;
        //endClock = identify when this finished
        //calculate amount of time spent finding the closets points
        //display wich ones are the closets points and their distance and how much time it took to find the closets points
        //end

        /*
var t0 = performance.now();
doSomething();
var t1 = performance.now();
console.log("Call to doSomething took " + (t1 - t0) + " milliseconds.")

        */
    }

    def findClosestPoints(pointsList){
        def c = 0.0;
        def smallestDistance = 0.0;
        def pointA, pointB;
        pointsList.each{ pA ->

            pointsList.each{ pB ->

                if(pA != pB){
                    def a = pA.x.abs() - pB.x.abs();
                    def b = pA.y.abs() - pB.y.abs();
                    c = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
                    if( c < smallestDistance || smallestDistance == 0){
                        smallestDistance = c;
                        pointA = pA;
                        pointB = pB;
                    } 
                }
            }
        }
        print smallestDistance;
        print pointA;
        print pointB;
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
        (Math.pow(squareIndex, 2)).times{
            def newPoint = new Point(random.nextInt(pointRestriction),random.nextInt(pointRestriction));
            pointsList.add(newPoint);
        }
        return pointsList;
    }

    def printPoints(pointsList){
        pointsList.each{ point->
            print point;
        }
    }

    def show(Point pointInstance) {
        respond pointInstance
    }

    def create() {
        respond new Point(params)
    }

    @Transactional
    def save(Point pointInstance) {
        if (pointInstance == null) {
            notFound()
            return
        }

        if (pointInstance.hasErrors()) {
            respond pointInstance.errors, view:'create'
            return
        }

        pointInstance.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.created.message', args: [message(code: 'point.label', default: 'Point'), pointInstance.id])
                redirect pointInstance
            }
            '*' { respond pointInstance, [status: CREATED] }
        }
    }

    def edit(Point pointInstance) {
        respond pointInstance
    }

    @Transactional
    def update(Point pointInstance) {
        if (pointInstance == null) {
            notFound()
            return
        }

        if (pointInstance.hasErrors()) {
            respond pointInstance.errors, view:'edit'
            return
        }

        pointInstance.save flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.updated.message', args: [message(code: 'Point.label', default: 'Point'), pointInstance.id])
                redirect pointInstance
            }
            '*'{ respond pointInstance, [status: OK] }
        }
    }

    @Transactional
    def delete(Point pointInstance) {

        if (pointInstance == null) {
            notFound()
            return
        }

        pointInstance.delete flush:true

        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'Point.label', default: 'Point'), pointInstance.id])
                redirect action:"index", method:"GET"
            }
            '*'{ render status: NO_CONTENT }
        }
    }

    protected void notFound() {
        request.withFormat {
            form multipartForm {
                flash.message = message(code: 'default.not.found.message', args: [message(code: 'point.label', default: 'Point'), params.id])
                redirect action: "index", method: "GET"
            }
            '*'{ render status: NOT_FOUND }
        }
    }
}
