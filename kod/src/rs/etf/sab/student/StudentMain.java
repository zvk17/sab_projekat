package rs.etf.sab.student;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import rs.etf.sab.operations.*;
import rs.etf.sab.tests.CityOperationsTest;
import rs.etf.sab.tests.CourierRequestOperationTest;
import rs.etf.sab.tests.Pair;
import rs.etf.sab.tests.PublicModuleTest;
import rs.etf.sab.tests.StockroomOperationsTest;
import rs.etf.sab.tests.TestHandler;
import rs.etf.sab.tests.TestRunner;
import rs.etf.sab.tests.UserOperationsTest;
import rs.etf.sab.tests.Util;
import rs.etf.sab.tests.VehicleOperationsTest;


public class StudentMain {

    public static void main(String[] args) {
    	
        AddressOperations addressOperations = new MyAddressOperations();//done // Change this to your implementation.
        CityOperations cityOperations = new MyCityOperations();//done // Do it for all classes.
        CourierOperations courierOperations = new MyCourierOperations();//done // e.g. = new MyDistrictOperations();
        CourierRequestOperation courierRequestOperation = new MyCourierRequestOperation();//done
        DriveOperation driveOperation = new MyDriveOperation();
        GeneralOperations generalOperations = new MyGeneralOperations(); //done
        PackageOperations packageOperations = new MyPackageOperations();//done
        StockroomOperations stockroomOperations = new MyStockroomOperations();//done
        UserOperations userOperations = new MyUserOperations();//done
        VehicleOperations vehicleOperations = new MyVehicleOperations();//todo parking


        TestHandler.createInstance(
                addressOperations,
                cityOperations,
                courierOperations,
                courierRequestOperation,
                driveOperation,
                generalOperations,
                packageOperations,
                stockroomOperations,
                userOperations,
                vehicleOperations
        );

        //StudentMain.testUser();
        //StudentMain.testStockroomOperations();
        //StudentMain.testVehicleOps();
        //StudentMain.testCourierReqOps();      
        //StudentMain.testModules();
                
        TestRunner.runTests();
        
       
    }
    
    private static void testModules() {
		// TODO Auto-generated method stub
    	new MyGeneralOperations().eraseAll();
    	PublicModuleTest test = new PublicModuleTest();
    	
    	test.setUp();
    	//test.publicOne();
    	test.tearUp();
    	
    	test.setUp();
    	test.publicTwo();
    	test.tearUp();
	}
	private static void testCourierReqOps() {
		// TODO Auto-generated method stub
    	new MyGeneralOperations().eraseAll();
    	CourierRequestOperationTest test = new CourierRequestOperationTest();
    	
    	test.setUp();
    	test.insertCourierRequest();
    	test.tearDown();
    	
    	test.setUp();
    	test.insertCourierRequest_NoUser();
    	test.tearDown();
    	
    	test.setUp();
    	test.insertCourierRequest_RequestExists();
    	test.tearDown();
    	
    	test.setUp();
    	test.insertCourierRequest_AlreadyCourier();
    	test.tearDown();
	
    
    	test.setUp();
    	test.grantRequest();
    	test.tearDown();
    	
    	test.setUp();
    	test.grantRequest_NoRequest();
    	test.tearDown();
    	
    	
    }
	private static void testVehicleOps() {
    	new MyGeneralOperations().eraseAll();
    	VehicleOperationsTest test = new VehicleOperationsTest();
    	
    	test.setUp();
    	test.insertVehicle();
    	test.tearDown();
    	
    	
    	test.setUp();
    	test.insertVehicle();
    	test.tearDown();
    	
    	test.setUp();
    	test.insertVehicle_UniqueLicencePlateNumber();
    	test.tearDown();
    	
    	test.setUp();
    	test.deleteVehicles();
    	test.tearDown();
    	
    	
    	test.setUp();
    	test.parkVehicle();
    	test.tearDown(); 
    	
    	test.setUp();
    	test.parkVehicle_NoStockroom();
    	test.tearDown();  
    	
    	test.setUp();
    	test.changeFuelType();
    	test.tearDown();  
    	
    	test.setUp();
    	test.changeConsumption();
    	test.tearDown(); 
    	
    	test.setUp();
    	test.changeCapacity();
    	test.tearDown(); 
		
	}
	private static void testStockroomOperations() {
    	new MyGeneralOperations().eraseAll();
    	StockroomOperationsTest test = new StockroomOperationsTest();
    	
    	test.setUp();
    	test.insertStockroom_OnlyOne();
    	test.tearDown();
    	
    	test.setUp();
    	test.insertStockrooms_SameCity();
    	test.tearDown();
    	
    	
    	test.setUp();
    	test.insertStockrooms_DifferentCity();
    	test.tearDown();
    	
    	test.setUp();
    	test.deleteStockroom();
    	test.tearDown();
    	
    	test.setUp();
    	test.deleteStockroom_NoStockroom();
    	test.tearDown();
    	
    	
    	test.setUp();
    	test.deleteStockroomFromCity();
    	test.tearDown();
    	
    	
    	test.setUp();
    	test.deleteStockroomFromCity_NoCity();
    	test.tearDown();
    	
    	test.setUp();
    	test.deleteStockroomFromCity_NoStockroom();
    	test.tearDown();
    	
		
	}
	private static void testUser() {
    	new MyGeneralOperations().eraseAll();
        UserOperationsTest test = new UserOperationsTest();
    	
    	test.setUp();
    	test.insertUser_Good();
    	test.tearDown();
    	
    	test.setUp();
    	test.insertUser_UniqueUsername();
    	test.tearDown();
    	
    	test.setUp();
    	test.insertUser_BadFirstname();
    	test.tearDown();
    	
    	
    	test.setUp();
    	test.insertUser_BadLastName();
    	test.tearDown();
    	
    	test.setUp();
    	test.insertUser_BadAddress();
    	test.tearDown();
    	
    	test.setUp();
    	test.insertUser_BadPassword();
    	test.tearDown();
    	
    	
    	test.setUp();
    	test.declareAdmin();
    	test.tearDown();
        
    	test.setUp();
    	test.declareAdmin_AlreadyAdmin();
    	test.tearDown();
    	
    	test.setUp();
    	test.declareAdmin_NoSuchUser();
    	test.tearDown();
    	
    	test.setUp();
    	test.getSentPackages_userExisting();
    	test.tearDown();
    	
    	test.setUp();
    	test.getSentPackages_userNotExisting();
    	test.tearDown();
    	
    	test.setUp();
    	test.deleteUsers();
    	test.tearDown();
    	
    }
}
