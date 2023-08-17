package com.gp.view;

import com.gp.model.OrderDto;
import com.gp.model.ProductDto;
import com.gp.model.TaxDto;
import com.gp.service.*;

import javax.sound.midi.Soundbank;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLOutput;
import java.time.LocalDate;
import java.util.*;

public class FlooringMasteryView {

    static Scanner input = new Scanner(System.in);

    public void runMenu() {

        ProductService productService = null;
        try {
            productService = new ProductServiceImpl();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TaxService taxService = null;
        try {
            taxService = new TaxServiceImpl();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        OrderService orderService = null;
        try { //deal with the propagated exception
            orderService = new OrderServiceImpl();
        } catch (IOException e) {
            System.out.println("There was an error in reading the file. Please try again.");
        }

        char continueOption = 'n';
        do {
            System.out.println(" * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
            System.out.println("* <<Flooring Program>>");
            System.out.println("* 1. Display Orders");
            System.out.println("* 2. Add an Order");
            System.out.println("* 3. Edit an Order");
            System.out.println("* 4. Remove an Order");
            System.out.println("* 5. Export All Data"); //no longer needed
            System.out.println("* 6. Quit");
            System.out.println("*");
            System.out.println(" * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");

            System.out.print("Please select your option:");
            try {
                int userOption = input.nextInt();


                switch (userOption) {
                    case 1:
                        System.out.println("Enter the date for which you wish to display the orders:");
                        System.out.println("YYYY\n" +
                                "MM\n" +
                                "DD");
                        LocalDate orderDate = LocalDate.of(input.nextInt(), input.nextInt(), input.nextInt());
                        try {
                            List<OrderDto> printOrders = orderService.getAlLOrdersByDate(orderDate);
                            if (printOrders.isEmpty()) {
                                System.out.println("Sorry, there are no existing orders for that date.");
                                break;
                            }
                            System.out.println("Order(s) retrieved:");
                            // System.out.println("OrderNumber,CustomerName,State,TaxRate,ProductType,Area,CostPerSquareFoot,LaborCostPerSquareFoot,MaterialCost,LaborCost,Tax,Total");
                            //print out order's values one by one, on separate lines, commenting what they represent, to enrich the user experience
                            for (OrderDto order : printOrders) {
                                System.out.println("Order number: " + order.getOrderNumber() + "\nCustomer name: " + order.getCustomerName()
                                        + "\nState: " + order.getTaxDetails().getStateAbbreviation()
                                        + "\nTax rate: " + order.getTaxDetails().getTaxRate()
                                        + "\nProduct type: " + order.getProductDetails().getProductType() + "\nArea: " + order.getArea()
                                        + "\nCost per square foot: " + order.getProductDetails().getCostPerSquareFoot()
                                        + "\nLabor cost per square foot" + order.getProductDetails().getLaborCostPerSquareFoot()
                                        + "\nMaterial cost: " + order.getMaterialCost() + "\nLabor cost: " + order.getLaborCost()
                                        + "\nTax: " + order.getTax() + "\nTotal: " + order.getTotal());
                            }
                        } catch (IOException e) {
                            System.out.println("Sorry, there are no existing orders for that date.");
                            break;
                        }
                        break;

                    case 2:
                        //tax and product objects, which make part of the order object
                        TaxDto taxDetails = null;
                        ProductDto productDetails = null;
                        OrderDto myOrderDto = null;


                        System.out.println("Let's enter the new order details!");

                        System.out.println("First, enter the order's date: (YYYY-MM-DD)");
                        input.nextLine();
                        String dateString = input.nextLine();
                        LocalDate orderDtoDate = LocalDate.parse(dateString); //parse

                        //customer name
                        System.out.println("Enter customer name:");
                        String customerName = input.nextLine();
                        if (customerName.isEmpty()) { //validation
                            System.out.println("Sorry, customer name cannot be empty. Redirecting you to main menu...");
                            runMenu();
                        }

                        //TAXES
                        //call getAllTaxInfo() from TaxService , which will return a map
                        Map<String, BigDecimal> taxInfoCollection = taxService.getAllTaxInfo();
                        // get the Set of keys from the map
                        Set<String> keys = taxInfoCollection.keySet();

                        //display choices to the user and prompt to choose one of the options
                        System.out.println("Enter Tax State: Option are limited to : " + keys);
                        String state = input.nextLine();

                        //check if the user input matches a map key
                        for (String k : keys) {
                            if (k.equals(state)) { //if yes, get the key ad value and store them in a TaxDto
                                System.out.println("Ok. Order state has been set to:" + k +
                                        ", with a tax rate value of: " + taxInfoCollection.get(k) + ".\n");
                                taxDetails = new TaxDto(k, taxInfoCollection.get(k));
                            }
                        }
                        //if the input does not match a key, display msg and redirect to main menu
                        if (taxDetails == null) {
                            System.out.println("Sorry, this state is not on the list! Redirecting you back to main menu...");
                            runMenu();
                        }
                        //  System.out.println("These are the tax details: " + taxDetails); //just for testing

                        //PRODUCT
                        System.out.println("Next, here is the list of available products and pricing information to choose from.");
                        Map<String, ProductDto> productInfoMap = productService.getAllProductsInfo();
                        // get the Set of keys from the map
                        Set<String> productKeys = productInfoMap.keySet();
                        // print the keys to the screen - to allow user to choose the product
                        for (String k : productKeys) {
                            System.out.println("Option " + k + ": " + productInfoMap.get(k));
                        }
                        System.out.println("Please type in the product name you wish to add to your order (case sensitive):");
                        String productChoice = input.nextLine();
                        for (String k : productKeys) {
                            if (k.equals(productChoice)) { //if yes, get the key ad value and store them in a TaxDto
                                System.out.println("Ok. Product details have been set to:" + productInfoMap.get(k).getProductType() +
                                        ", with " + productInfoMap.get(k).getCostPerSquareFoot() + " cost per square foot and " +
                                        productInfoMap.get(k).getLaborCostPerSquareFoot() + " labor cost per square foot.\n");
                                productDetails = new ProductDto(k, productInfoMap.get(k).getCostPerSquareFoot(), productInfoMap.get(k).getLaborCostPerSquareFoot());
                            }
                        }
                        //if the input does not match a key (meaning that the productDetails object is null)
                        // display msg and redirect to main menu
                        if (productDetails == null) {
                            System.out.println("Sorry, this product is not on the list! Redirecting you back to main menu...");
                            runMenu();
                        }

                        //Area
                        System.out.println("Next, please input the area (minimum 100)");
                        int areaInt = input.nextInt();
                        if (areaInt < 100) {
                            System.out.println("Sorry, area requirements have not been met(minimum 100).");
                            System.out.println("Redirecting you back to main menu....");
                            runMenu();
                        }
                        BigDecimal area = BigDecimal.valueOf(areaInt); //convert int to BigDecimal

                        //material cost = area * costPerSquareFoot - BigDecimal does not take "*" as operand; instead multiply();
                        // call getCistPerSquareFoot() of the above created ProductDto
                        BigDecimal materialCost = area.multiply(productDetails.getCostPerSquareFoot());

                        //similarly to laborCost
                        BigDecimal laborCost = area.multiply(productDetails.getLaborCostPerSquareFoot());

                        //tax = (materialCost + laborCost) * (taxRate/100) - will call TaxDto this time
                        // transfor int 100 to a BigDcimal in oredr to perform division
                        BigDecimal tax = (materialCost.add(laborCost)).multiply(taxDetails.getTaxRate().divide(BigDecimal.valueOf(100)));

                        //total
                        BigDecimal total = materialCost.add(laborCost).add(tax);

                        //set OrderNumber initially to a dummy value,
                        //however this will be checked upon calling the addOrder(), which check if there are other orders on the same date
                        //if this is the first order, it will assign value 1 , else it will increment the orderNumber comparing the last entered order id
                        int orderNumber = 0;

                        // form the order DTO
                        myOrderDto = new OrderDto(orderNumber, customerName, taxDetails, productDetails, area, materialCost, laborCost, tax, total);

                        //set the order date
                        myOrderDto.setOrderDate(orderDtoDate);

                        System.out.println("Here are the new order details: ");
                        //Refactored myOrderDto.toString() , for a more friendly user experience
                        System.out.println("Customer name: " + myOrderDto.getCustomerName()
                                + "\nState: " + myOrderDto.getTaxDetails().getStateAbbreviation()
                                + "\nTax rate: " + myOrderDto.getTaxDetails().getTaxRate()
                                + "\nProduct type: " + myOrderDto.getProductDetails().getProductType() + "\nArea: " + myOrderDto.getArea()
                                + "\nCost per square foot: " + myOrderDto.getProductDetails().getCostPerSquareFoot()
                                + "\nLabor cost per square foot" + myOrderDto.getProductDetails().getLaborCostPerSquareFoot()
                                + "\nMaterial cost: " + myOrderDto.getMaterialCost() + "\nLabor cost: " + myOrderDto.getLaborCost()
                                + "\nTax: " + myOrderDto.getTax() + "\nTotal: " + myOrderDto.getTotal()
                                + ", for " + orderDtoDate + ".");

                        System.out.println("Do you wish to add this order to the system? y/n");
                        char choice = input.next().charAt(0);
                        if (choice == 'y') {
                            try {
                                orderService.addNewOrder(myOrderDto);
                                System.out.println("Order added to the system.");
                            }// IOException handling
                            catch (IOException e) {
                                e.printStackTrace();
                                System.out.println("Something went wrong when trying to save the order to file. Please try again.");
                            }
                            ;
                        } else {
                            System.out.println("Adding new order was cancelled. Redirecting you to the main menu...");
                            runMenu();
                        }
                        break;
                    case 3:
                        System.out.println("Edit an order:");
                        break;
                    case 4:
                        System.out.println("Remove an order:");
                        break;
                    case 5:
                        System.out.println("Export All Data:");
                        break;
                    case 6:
                        System.out.println("Quit");
                        System.out.println("Are you sure you wish to quit now? Press y to confirm.");
                        char quitOption = input.next().charAt(0);
                        if (quitOption == 'y') {
                            System.exit(0);
                        } else {
                            System.out.println("Ok. Redirecting you to main menu...");
                            runMenu();
                        }
                    default:
                        System.out.println("Invalid option. Please choose a value between 1- 6, according to the menu.");
                        runMenu();
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid option. Program needs to be restarted!");
                System.exit(1);
            }
            System.out.println("Do you want to continue? y/n");
            continueOption = input.next().charAt(0);
        }
        while (continueOption == 'y');


        /*
         // TAX SERVICE methods testing
        TaxService taxService2=null;
        System.out.println("your tax info is as following:");
        try {
            taxService2 = new TaxServiceImpl();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("your tax info for the orders is as following:");

        Map<String, BigDecimal> showTaxInfo =taxService2.getAllTaxInfo();
        if (showTaxInfo.isEmpty()){
            System.out.println("There are no entries in the Taxes information file.");
        } else {
            // get the Set of keys from the map
            Set<String> keys = showTaxInfo.keySet();

            // print the keys to the screen
            for (String k : keys) {
                System.out.println ( k + " " + showTaxInfo.get(k));
            }
        }


        //PRODUCT TESTING
        ProductService productService;
        System.out.println("Your products' info is as following:");

        try {
            productService= new ProductServiceImpl();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Assuming data has been read from the file, info is as following:");

        Map<String, ProductDto> showProductInfo =productService.getAllProductsInfo();

        if (showProductInfo.isEmpty()){
            System.out.println("There are no entries in the Products information file.");
        } else {
            // get the Set of keys from the map
            Set<String> keys = showProductInfo.keySet();
            // print the keys to the screen
            for (String k : keys) {
                System.out.println ("Key product "+ k + ": " + showProductInfo.get(k));
            }
        }


        //ORDER TESTING #

        OrderService orderService;
        System.out.println("Your today's date orders are as following:");
        try {
            orderService= new OrderServiceImpl();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //THIS WAS THE MISTAKE - Wrong Data format Input
        // LocalDate orderDate= LocalDate.ofEpochDay(2023-06-14);

        LocalDate orderDate=LocalDate.now();
        List<OrderDto> printOrders = orderService.getAlLOrdersByDate(LocalDate.of(2023,06,14));
        for(OrderDto order: printOrders){
            System.out.println(order);
        }
        System.out.println(printOrders.size());
*/

    }
}
