package assignment1

class Point {

	def x;
	def y;

    static constraints = {
    	x matches: "[0-9]+"
    	y matches: "[0-9]+"
    }

    def String toString(){
    	return "(" + x + ", " + y + ")";
    }

    Point(px, py){
    	x = px;
    	y = py;
    }
}
