import java.util.ArrayList;
import java.util.HashMap;

import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import static spark.Spark.*;
import java.util.List;

public class App {
  public static void main(String[] args) {
    setPort(getHerokuPort());
      staticFileLocation("/public");
      String layout = "templates/layout.vtl";      
    
      //ROUTES: GETTING HOME PAGE

      get("/", (request, response) -> {
        HashMap<String, Object> model = new HashMap<String, Object>();

        model.put("client", Client.class);
        model.put("stylist", Stylist.class);
        model.put("stylists", Stylist.all());
        model.put("unassignedclients", Client.unassignedClientsExist());
        model.put("template", "templates/index.vtl");
        return new ModelAndView(model, layout);
      }, new VelocityTemplateEngine());

      //ROUTES: GETTING RESOURCES

      get("/stylists/:id", (request, response) -> {
        HashMap<String, Object> model = new HashMap<String, Object>();
        Stylist thisStylist = Stylist.find(
          Integer.parseInt(request.params("id")));

        model.put("stylist", thisStylist);
        model.put("clients", thisStylist.getAllClients());
        model.put("template", "templates/stylist.vtl");
        return new ModelAndView(model, layout);
      }, new VelocityTemplateEngine());

      get("/clients/:id", (request, response) -> {
        HashMap<String, Object> model = new HashMap<String, Object>();
        Client thisClient = Client.find(
          Integer.parseInt(request.params("id")));
        Stylist currentStylist = Stylist.find(thisClient.getStylistId());

        model.put("stylist", Stylist.class);
        model.put("currentstylist", currentStylist);
        model.put("client", thisClient);
        model.put("template", "templates/client.vtl");
        return new ModelAndView(model, layout);
      }, new VelocityTemplateEngine());

      //ROUTES: CHANGING RESOURCES

      //Create a stylist
      post("/new-stylist", (request, response) -> {
        HashMap<String, Object> model = new HashMap<String, Object>();

        String requestedFirst = request.queryParams("firstname");
        String requestedLast = request.queryParams("lastname");
        Stylist requestedStylist = new Stylist(requestedFirst, requestedLast);
        boolean duplicateStylistRequested = requestedStylist.isDuplicate();
        if (!(duplicateStylistRequested)) {
          requestedStylist.save();
        } else {
          model.put("duplicatestylistrequested", duplicateStylistRequested);
        }

        model.put("client", Client.class);
        model.put("stylist", Stylist.class);
        model.put("stylists", Stylist.all());
        model.put("unassignedclients", Client.unassignedClientsExist());
        model.put("template", "templates/index.vtl");
        return new ModelAndView(model, layout);
      }, new VelocityTemplateEngine());

      //Delete a stylist
      post("/remove-stylist", (request, response) -> {
        HashMap<String, Object> model = new HashMap<String, Object>();

        Stylist removalRequest = Stylist.find(Integer.parseInt(
          request.queryParams("removestylist")));
        removalRequest.delete();

        response.redirect("/");
        return null;
      });

      //Update a stylist
      post("/stylists/:id/update", (request, response) -> {
        HashMap<String, Object> model = new HashMap<String, Object>();
        Stylist thisStylist = Stylist.find(
          Integer.parseInt(request.params("id")));

        String requestedFirst = request.queryParams("newfirstname");
        String requestedLast = request.queryParams("newlastname");
        Stylist duplicateChecker = new Stylist(requestedFirst, requestedLast);
        boolean duplicateStylistRequested = duplicateChecker.isDuplicate();
        if (!(duplicateStylistRequested)) {
          thisStylist.update(requestedFirst, requestedLast);
        } else {
          model.put("duplicatestylistrequested", duplicateStylistRequested);
        }

        model.put("stylist", thisStylist);
        model.put("clients", thisStylist.getAllClients());
        model.put("template", "templates/stylist.vtl");
        return new ModelAndView(model, layout);
      }, new VelocityTemplateEngine());

      //Create a client
      post("/stylists/:id/new-client", (request, response) -> {
        HashMap<String, Object> model = new HashMap<String, Object>();
        Stylist thisStylist = Stylist.find(
          Integer.parseInt(request.params("id")));

        String requestedFirst = request.queryParams("firstname");
        String requestedLast = request.queryParams("lastname");
        Client requestedClient = new Client(requestedFirst, requestedLast);
        boolean duplicateClientRequested = requestedClient.isDuplicate();
        if (!(duplicateClientRequested)) {
          requestedClient.save();
          requestedClient.assignStylist(thisStylist.getId());
        } else {
          model.put("duplicateclientrequested", duplicateClientRequested);
        }

        model.put("stylist", thisStylist);
        model.put("clients", thisStylist.getAllClients());
        model.put("template", "templates/stylist.vtl");
        return new ModelAndView(model, layout);
      }, new VelocityTemplateEngine());

      //Delete a client
      post("/stylists/:id/remove-client", (request, response) -> {
        HashMap<String, Object> model = new HashMap<String, Object>();
        Stylist thisStylist = Stylist.find(
          Integer.parseInt(request.params("id")));

        Client removalRequest = Client.find(Integer.parseInt(
            request.queryParams("removeclient")));
        removalRequest.delete();

        response.redirect("/stylists/" + thisStylist.getId());
        return null;
      });

      //Update a client - change name
      post("/clients/:id/update", (request, response) -> {
        HashMap<String, Object> model = new HashMap<String, Object>();
        Client thisClient = Client.find(
          Integer.parseInt(request.params("id")));
        Stylist currentStylist = Stylist.find(thisClient.getStylistId());

        String requestedFirst = request.queryParams("newfirstname");
        String requestedLast = request.queryParams("newlastname");
        Client duplicateChecker = new Client(requestedFirst, requestedLast);
        boolean duplicateClientRequested = duplicateChecker.isDuplicate();
        if (!(duplicateClientRequested)) {
          thisClient.update(requestedFirst, requestedLast);
        } else {
          model.put("duplicateclientrequested", duplicateClientRequested);
        }

        model.put("stylist", Stylist.class);
        model.put("currentstylist", currentStylist);
        model.put("client", thisClient);
        model.put("template", "templates/client.vtl");
        return new ModelAndView(model, layout);
      }, new VelocityTemplateEngine());

      //Update a client - change stylist
      post("/clients/:id/assign-stylist", (request, response) -> {
        HashMap<String, Object> model = new HashMap<String, Object>();
        Client thisClient = Client.find(
          Integer.parseInt(request.params("id")));
        Stylist currentStylist = Stylist.find(thisClient.getStylistId());


        thisClient.assignStylist(Integer.parseInt(
          request.queryParams("newstylistid")));
        currentStylist = Stylist.find(thisClient.getStylistId());

        response.redirect("/clients/" + thisClient.getId());
        return null;
      });
    }
    
    static int getHerokuPort(){
        ProcessBuilder process = new ProcessBuilder();
        Integer port;

        if (process.environment().get("PORT") != null) {
            return port = Integer.parseInt(process.environment().get("PORT"));
        }
        return 4567; //return default port if heroku-port isn't set (i.e. on localhost)
    }

}
