import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;

public class AutoDrone {

    int map_size = 3000;
    enum PixelState {blocked,explored,unexplored,visited};
    ArrayList<Operation> operations;
    PixelState map[][];
    Drone drone;
    Point droneStartingPoint;
    ArrayList<Point> points;
    int isRotating;
    ArrayList<Double> degrees_left;
    ArrayList<Func> degrees_left_func;
    boolean isSpeedUp = false;
    Graph mGraph = new Graph();
    CPU ai_cpu;
    boolean is_init = true;
    double lastFrontLidarDis = 0;
    boolean isRotateRight = false;
    double changedRight = 0;
    double changedLeft = 0;
    boolean tryToEscape = false;
    int leftOrRight = 1;
    double max_rotation_to_direction = 20;
    boolean  is_finish = true;
    boolean isLeftRightRotationEnable = true;
    boolean is_risky = false;
    int max_risky_distance = 100; // we changed here to 100 from 150
    boolean try_to_escape = false;
    double  risky_dis = 0;
    int max_angle_risky = 10;
    boolean is_lidars_max = false;
    double save_point_after_seconds = 3;
    double max_distance_between_points = 100;
    boolean start_return_home = false;
    Point init_point;
    double maxLidarDistance = 300; //3 Meter
    boolean hugLeftWall;
    int timesTurned;
    double orient;


    public AutoDrone(Map realMap) {
        degrees_left = new ArrayList<>();
        degrees_left_func =  new ArrayList<>();
        points = new ArrayList<Point>();
        operations = new ArrayList<>();
        hugLeftWall = true;
        timesTurned = 0;
        orient = 0;

        drone = new Drone(realMap);
        drone.addLidar(0);
        drone.addLidar(90);
        drone.addLidar(-90);

        initMap();

        isRotating = 0;
        ai_cpu = new CPU(200,"Auto_AI");
        ai_cpu.addFunction(this::update);
    }

    public void initMap() {
        map = new PixelState[map_size][map_size];
        for(int i=0;i<map_size;i++) {
            for(int j=0;j<map_size;j++) {
                map[i][j] = PixelState.unexplored;
            }
        }

        droneStartingPoint = new Point(map_size/2,map_size/2,drone.getGyroRotation());
    }

    public void play() {
        drone.play();
        ai_cpu.play();
    }

    public void update(int deltaTime) {
        updateVisited();
        updateMapByLidars();
        ai(deltaTime);
        if(isRotating != 0) {
            updateRotating(deltaTime);
        }
        if(isSpeedUp) {
            drone.speedUp(deltaTime);
        } else {
            drone.slowDown(deltaTime);
        }
    }

    public void speedUp() {
        isSpeedUp = true;
    }

    public void speedDown() {
        isSpeedUp = false;
    }

    public void updateMapByLidars() {
        Point dronePoint = drone.getOpticalSensorLocation();
        Point fromPoint = new Point(dronePoint.x + droneStartingPoint.x,dronePoint.y + droneStartingPoint.y, drone.getGyroRotation());

        for(int i=0;i<drone.lidars.size();i++) {
            Lidar lidar = drone.lidars.get(i);
            double rotation = drone.getGyroRotation() + lidar.degrees;
            //rotation = Drone.formatRotation(rotation);
            for(int distanceInCM=0;distanceInCM < lidar.current_distance;distanceInCM++) {
                Point p = Tools.getPointByDistance(fromPoint, rotation, distanceInCM);
                setPixel(p.x,p.y,PixelState.explored);
            }

            if(lidar.current_distance > 0 && lidar.current_distance < WorldParams.lidarLimit - WorldParams.lidarNoise) {
                Point p = Tools.getPointByDistance(fromPoint, rotation, lidar.current_distance);
                setPixel(p.x,p.y,PixelState.blocked);
                //fineEdges((int)p.x,(int)p.y);
            }
        }
    }

    public void updateVisited() {
        Point dronePoint = drone.getOpticalSensorLocation();
        Point fromPoint = new Point(dronePoint.x + droneStartingPoint.x,dronePoint.y + droneStartingPoint.y,drone.getGyroRotation());

        setPixel(fromPoint.x,fromPoint.y,PixelState.visited);

    }

    public void setPixel(double x, double y,PixelState state) {
        int xi = (int)x;
        int yi = (int)y;

        if(state == PixelState.visited) {
            map[xi][yi] = state;
            return;
        }

        if(map[xi][yi] == PixelState.unexplored) {
            map[xi][yi] = state;
        }
    }

    public void paintBlindMap(Graphics g) {
        Color c = g.getColor();

        int i = (int)droneStartingPoint.y - (int)drone.startPoint.x;
        int startY = i;
        for(;i<map_size;i++) {
            int j = (int)droneStartingPoint.x - (int)drone.startPoint.y;
            int startX = j;
            for(;j<map_size;j++) {
                if(map[i][j] != PixelState.unexplored)  {
                    if(map[i][j] == PixelState.blocked) {
                        g.setColor(Color.RED);
                    }
                    else if(map[i][j] == PixelState.explored) {
                        g.setColor(Color.YELLOW);
                    }
                    else if(map[i][j] == PixelState.visited) {
                        g.setColor(Color.BLUE);
                    }
                    g.drawLine(i-startY, j-startX, i-startY, j-startX);
                }
            }
        }
        g.setColor(c);
    }

    public void paintPoints(Graphics g) {
        for(int i=0;i<points.size();i++) {
            Point p = points.get(i);
            g.drawOval((int)p.x + (int)drone.startPoint.x - 10, (int)p.y + (int)drone.startPoint.y-10, 20, 20);
        }

    }

    public void paint(Graphics g) {
        if(SimulationWindow.toogleRealMap) {
            drone.realMap.paint(g);
        }

        paintBlindMap(g);
        paintPoints(g);

        drone.paint(g);


    }

    public void ai(int deltaTime) {
        if (!SimulationWindow.toogleAI) {
            return;
        }

//        if (!operations.isEmpty()) {
//            Operation o = operations.get(0);
//            System.out.println(o);
//            if (o.isFinished(drone.getOpticalSensorLocation())) {
//                operations.remove(0);
//            }
//            return;
//        }
        if (!(orient > drone.getGyroRotation() && Drone.formatRotation(orient-drone.getGyroRotation()) > -2 && Drone.formatRotation(orient-drone.getGyroRotation()) < 2)) {
           return; //dismiss
        }
        speedUp();
        Lidar lidarF = drone.lidars.get(0);
        Lidar lidarR = drone.lidars.get(1);
        Lidar lidarL = drone.lidars.get(2);
        if (lidarF.getDistance(deltaTime) < 200) {
            speedDown();
            spinBy(45);
//            Point target = drone.getOpticalSensorLocation();
//            Operation op = new Operation(OperationType.flying, Tools.getPointByDistance(drone.getOpticalSensorLocation(), 45, 0));
//            operations.add(op);

        }
        /*
        if (lidarL.getDistance(deltaTime) > maxLidarDistance) {
            speedDown();
            spinBy(-45);
            Point target = drone.getOpticalSensorLocation();
            Operation op = new Operation(OperationType.flying, Tools.getPointByDistance(drone.getOpticalSensorLocation(), -45, 0));
            operations.add(op);
        }
        */

        /*
        System.out.println("drone: " +drone.getOpticalSensorLocation().toString() + ", gyro: " + drone.getGyroRotation());
        if (!operations.isEmpty()) {
            Operation running = operations.get(0);
            if (!running.isFinished(drone.getOpticalSensorLocation())) {
                System.out.println("not finished" + operations.get(0).toString());
                //speedDown();
                if (running.t == OperationType.flying && Math.abs(running.nextPoint.orient - drone.getGyroRotation()) < 0.5) {
                    operations.remove(0);
                }
            } else {
                operations.remove(0);
                //speedUp();
            }
            return;
        }


        Lidar lidarF = drone.lidars.get(0);
        Lidar lidarR = drone.lidars.get(1);
        Lidar lidarL = drone.lidars.get(2);
        if (hugLeftWall) {
            if (lidarL.getDistance(deltaTime) < 250) {
                if (lidarF.getDistance(deltaTime) > 200) {
                    speedUp();
                    Point target = drone.getOpticalSensorLocation();
                    Operation op = new Operation(OperationType.flying, Tools.getPointByDistance(drone.getOpticalSensorLocation(), 0, 50));
                    operations.add(op);
                    return;
                }else {
                    spinBy(90); // right
                    Point newPoint = new Point(drone.getOpticalSensorLocation());
                    newPoint.orient = Drone.formatRotation(newPoint.orient + 90);
                    Operation op = new Operation(OperationType.rotating, newPoint);
                    operations.add(op);
                    return;
                }
            }else {
                if (lidarR.getDistance(deltaTime) < lidarL.getDistance(deltaTime) && lidarL.getDistance(deltaTime) > 50) {
                    spinBy(-90); // left
                    Point newPoint = new Point(drone.getOpticalSensorLocation());
                    newPoint.orient = Drone.formatRotation(newPoint.orient - 90);
                    Operation op = new Operation(OperationType.rotating, newPoint);
                    operations.add(op);
                    return;
                }
            }
        }

         */
        /*
        if (lidarF.getDistance(deltaTime) > maxLidarDistance) {
            speedUp();
        }else if (lidarF.getDistance(deltaTime) < 50){
            speedDown();
            if (lidarR.getDistance(deltaTime) < lidarL.getDistance(deltaTime) && lidarL.getDistance(deltaTime) > 0.5) {
                spinBy(-90); // left
                Point newPoint = new Point(drone.getOpticalSensorLocation());
                newPoint.orient = Drone.formatRotation(newPoint.orient - 90);
                Operation op = new Operation(OperationType.rotating, newPoint);
                operations.add(op);
                return;
            }
            if (lidarR.getDistance(deltaTime) > lidarL.getDistance(deltaTime) && lidarR.getDistance(deltaTime) > 0.5) {
                spinBy(90); // right
                Point newPoint = new Point(drone.getOpticalSensorLocation());
                newPoint.orient = Drone.formatRotation(newPoint.orient + 90);
                Operation op = new Operation(OperationType.rotating, newPoint);
                operations.add(op);
                return;
            }
        }else {
            if (lidarR.getDistance(deltaTime) < lidarL.getDistance(deltaTime)) {
                if (lidarF.getDistance(deltaTime) < lidarL.getDistance(deltaTime)) {
                    spinBy(-30); // left
                    Point newPoint = new Point(drone.getOpticalSensorLocation());
                    newPoint.orient = Drone.formatRotation(newPoint.orient - 30);
                    Operation op = new Operation(OperationType.rotating, newPoint);
                    operations.add(op);
                    return;
                }
            } else {
                if (lidarF.getDistance(deltaTime) < lidarR.getDistance(deltaTime)) {
                    spinBy(30);
                    Point newPoint = new Point(drone.getOpticalSensorLocation());
                    newPoint.orient = Drone.formatRotation(newPoint.orient + 30);
                    Operation op = new Operation(OperationType.rotating, newPoint);
                    operations.add(op);
                    return;
                }
            }
        }
        */
        /*
        // if the Lidar is getting low distance- check right and left options
        if (lidarF.getDistance(deltaTime) < maxLidarDistance) {
            //speedDown();
            Lidar lidarR = drone.lidars.get(1);
            double right = lidarR.current_distance;

            Lidar lidarL = drone.lidars.get(2);
            double left = lidarL.current_distance;


            // find max Front, Left and Right
            if (lidarR.getDistance(deltaTime) < lidarL.getDistance(deltaTime)) {
                if (lidarF.getDistance(deltaTime) < lidarL.getDistance(deltaTime)) {
                    speedDown();
                    spinBy(-45); // left
                    Point newPoint = new Point(drone.getOpticalSensorLocation());
                    newPoint.orient -= 45;
                    Operation op = new Operation(OperationType.rotating, newPoint);
                    operations.add(op);
                }
            } else if (lidarF.getDistance(deltaTime) < lidarR.getDistance(deltaTime)) {
                speedDown();
                spinBy(45);
                Point newPoint = new Point(drone.getOpticalSensorLocation());
                newPoint.orient += 45;
                Operation op = new Operation(OperationType.rotating, newPoint);
                operations.add(op);
            } else {
                Point newPoint = new Point(drone.getOpticalSensorLocation());
                newPoint.x += Math.cos(newPoint.orient);
                newPoint.y += Math.sin(newPoint.orient);
                Operation op = new Operation(OperationType.flying, newPoint);
                operations.add(op);
            }
        }

         */






















            /*
            // Find the max distance direction (Front, Left, Right)
            if (lidarF.getDistance(deltaTime) > lidarR.getDistance(deltaTime)) {
                if (lidarF.getDistance(deltaTime) < lidarL.getDistance(deltaTime)) {
                    spinBy(-45); // take left
                    Point dronePoint = drone.getOpticalSensorLocation();
                    // update the Points arr
                    points.add(dronePoint);
                    mGraph.addVertex(dronePoint);
                }
                // stay front
                // continue straight 1 Meter
                Point dronePoint = drone.getOpticalSensorLocation();
                Point nextPoint = new Point(dronePoint.x + 100, dronePoint.y,drone.getGyroRotation()); // go 1 Meters to the front
                // update the Points arr
                points.add(dronePoint);
                mGraph.addVertex(dronePoint);

            } else {
                if (lidarR.getDistance(deltaTime) > lidarL.getDistance(deltaTime)) {
                    spinBy(45); // take right
                    Point dronePoint = drone.getOpticalSensorLocation();
                    // update the Points arr
                    points.add(dronePoint);
                    mGraph.addVertex(dronePoint);
                } else {
                    spinBy(-45); // take left
                    Point dronePoint = drone.getOpticalSensorLocation();
                    // update the Points arr
                    points.add(dronePoint);
                    mGraph.addVertex(dronePoint);

                }
            }

             */
        //}  //else {
////            // continue straight 2.5 Meters..
////            Point dronePoint = drone.getOpticalSensorLocation();
////            Point nextPoint = new Point(dronePoint.x + 250, dronePoint.y,drone.getGyroRotation()); // go 2.5 Meters to the front
////            Point thisPoint = new Point(dronePoint);
////            // update the Points arr
////            points.add(dronePoint);
////            mGraph.addVertex(dronePoint);
//        }
    }
//
//        if(is_init) {
//            speedUp();
//            Point dronePoint = drone.getOpticalSensorLocation();
//            init_point = new Point(dronePoint);
//            points.add(dronePoint);
//            mGraph.addVertex(dronePoint);
//            is_init = false;
//        }
//
//        Point dronePoint = drone.getOpticalSensorLocation();
//
//        if(SimulationWindow.return_home) {
//            // if the "Step" is good step..
//            if(Tools.getDistanceBetweenPoints(getLastPoint(), dronePoint) <  max_distance_between_points) {
//                // If the size of the points array is lower than 1- speed down because there is no points..
//                if(points.size() <= 1 && Tools.getDistanceBetweenPoints(getLastPoint(), dronePoint) <  max_distance_between_points/10) { // we changed here to /10
//                    speedDown();
//                } else {
//                    removeLastPoint();
//                }
//            }
//        } else {
//            if( Tools.getDistanceBetweenPoints(getLastPoint(), dronePoint) >=  max_distance_between_points) {
//                points.add(dronePoint);
//                mGraph.addVertex(dronePoint);
//            }
//        }
//
//        if(!is_risky) {
//
//            // updating the current distance between the drone and the front-wall
////            Lidar lidar = drone.lidars.get(0);
//
////            if(lidar.current_distance >= )
//
//                if(lidar.current_distance <= max_risky_distance ) {
//                    is_risky = true;
//                    risky_dis = lidar.current_distance;
//                }
//
//            Lidar lidar1 = drone.lidars.get(1);
//            if(lidar1.current_distance <= max_risky_distance/3 ) {
//                is_risky = true;
//            }
//
//            Lidar lidar2 = drone.lidars.get(2);
//            if(lidar2.current_distance <= max_risky_distance/3 ) {
//                is_risky = true;
//            }
//
//        } else {
//            // risky and you are not trying to escape- Try to escape!!!!
//            if(!try_to_escape) {
//                try_to_escape = true;
//                Lidar lidar1 = drone.lidars.get(1);
//                double a = lidar1.current_distance;
//
//                Lidar lidar2 = drone.lidars.get(2);
//                double b = lidar2.current_distance;
//
//                int spin_by = max_angle_risky;
//
//                if(a > 270 && b > 270) {
//                    is_lidars_max = true;
//                    Point l1 = Tools.getPointByDistance(dronePoint, lidar1.degrees + drone.getGyroRotation(), lidar1.current_distance);
//                    Point l2 = Tools.getPointByDistance(dronePoint, lidar2.degrees + drone.getGyroRotation(), lidar2.current_distance);
//                    Point last_point = getAvgLastPoint();
//                    double dis_to_lidar1 = Tools.getDistanceBetweenPoints(last_point,l1);
//                    double dis_to_lidar2 = Tools.getDistanceBetweenPoints(last_point,l2);
//
//                    if(SimulationWindow.return_home) {
//                        if( Tools.getDistanceBetweenPoints(getLastPoint(), dronePoint) <  max_distance_between_points) {
//                            removeLastPoint();
//                        }
//                    } else {
//                        if( Tools.getDistanceBetweenPoints(getLastPoint(), dronePoint) >=  max_distance_between_points) {
//                            points.add(dronePoint);
//                            mGraph.addVertex(dronePoint);
//                        }
//                    }
//
//                    spin_by = 90;
//
//                    // take the back side
//                    if(SimulationWindow.return_home) {
//                        spin_by *= -1;
//                    }
//
//                    // take the left side <<
//                    if(dis_to_lidar1 < dis_to_lidar2) {
//                        spin_by *= (-1 );
//                    }
//                } else {
//                    // take the right side >>
//                    if(a < b ) {
//                        spin_by *= (-1 );
//                    }
//                }
//
//                spinBy(spin_by,true,new Func() {
//                    @Override
//                    public void method() {
//                        try_to_escape = false;
//                        is_risky = false;
//                    }
//                });
//            }
//        }
//
//        //}

    int counter = 0;

    public void doLeftRight() {
        if(is_finish) {
            leftOrRight *= -1;
            counter++;
            is_finish = false;

            spinBy(max_rotation_to_direction*leftOrRight,false,new Func() {
                @Override
                public void method() {
                    is_finish = true;
                }
            });
        }
    }


    double lastGyroRotation = 0;
    public void updateRotating(int deltaTime) {

        if(degrees_left.size() == 0) {
            return;
        }

        double degrees_left_to_rotate = degrees_left.get(0);
        boolean isLeft = true;
        if(degrees_left_to_rotate > 0) {
            isLeft = false;
        }

        double curr =  drone.getGyroRotation();
        double just_rotated = 0;

        if(isLeft) {

            just_rotated = curr - lastGyroRotation;
            if(just_rotated > 0) {
                just_rotated = -(360 - just_rotated);
            }
        } else {
            just_rotated = curr - lastGyroRotation;
            if(just_rotated < 0) {
                just_rotated = 360 + just_rotated;
            }
        }



        lastGyroRotation = curr;
        degrees_left_to_rotate-=just_rotated;
        degrees_left.remove(0);
        degrees_left.add(0,degrees_left_to_rotate);

        if((isLeft && degrees_left_to_rotate >= 0) || (!isLeft && degrees_left_to_rotate <= 0)) {
            degrees_left.remove(0);

            Func func = degrees_left_func.get(0);
            if(func != null) {
                func.method();
            }
            degrees_left_func.remove(0);


            if(degrees_left.size() == 0) {
                isRotating = 0;
            }
            return;
        }

        int direction = (int)(degrees_left_to_rotate / Math.abs(degrees_left_to_rotate));
        drone.rotateLeft(deltaTime * direction);

    }

    public void spinBy(double degrees,boolean isFirst,Func func) {
        lastGyroRotation = drone.getGyroRotation();
        if(isFirst) {
            degrees_left.add(0,degrees);
            degrees_left_func.add(0,func);
        } else {
            degrees_left.add(degrees);
            degrees_left_func.add(func);
        }

        isRotating =1;
    }

    public void spinBy(double degrees,boolean isFirst) {
        lastGyroRotation = drone.getGyroRotation();
        if(isFirst) {
            degrees_left.add(0,degrees);
            degrees_left_func.add(0,null);


        } else {
            degrees_left.add(degrees);
            degrees_left_func.add(null);
        }

        isRotating =1;
    }

    public void spinBy(double degrees) {
        lastGyroRotation = drone.getGyroRotation();

        degrees_left.add(degrees);
        degrees_left_func.add(null);
        isRotating = 1;
    }

    // Getting the last point if exists
    public Point getLastPoint() {
        if(points.size() == 0) {
            return init_point;
        }

        Point p1 = points.get(points.size()-1);
        return p1;
    }

    // Removing the last point if exists
    public Point removeLastPoint() {
        if(points.isEmpty()) {
            return init_point;
        }
        return points.remove(points.size()-1);
    }


    public Point getAvgLastPoint() {
        if(points.size() < 2) {
            return init_point;
        }

        Point p1 = points.get(points.size()-1);
        Point p2 = points.get(points.size()-2);
        return new Point((p1.x + p2.x)/2, (p1.y + p2.y)/2,drone.getGyroRotation());
    }


}
