package fhirconverter.spark;

import ca.uhn.fhir.parser.DataFormatException;
import com.github.fge.jsonpatch.JsonPatch;
import fhirconverter.ConverterOpenempi;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hl7.fhir.dstu3.model.Patient;
import org.json.JSONObject;
import org.json.XML;
import spark.*;

import java.io.IOException;

import static spark.Spark.stop;

public class PatientController {
    private static Logger LOGGER = LogManager.getLogger(Patient.class);

    private static FHIRParser parser = new FHIRParser(Patient.class);

    private static JSONObject parseResource(String data, Representation format) throws DataFormatException, ClassCastException
    {
        if (format == Representation.JSON)
            return  parser.parseJSONResource(data);
        else if (format == Representation.XML)
            return parser.parseXMLResource(data);
        else
        {
            LOGGER.fatal("Impossible value for representation!!!");
            stop();
            return null;
        }
    }

    private static JsonPatch parsePatch(String data, Representation format) throws IOException
    {
        if (format == Representation.JSON)
            return  parser.parseJSONPatch(data);
        else if (format == Representation.XML)
            throw new DataFormatException();
        else
        {
            LOGGER.fatal("Impossible value for representation!!!");
            stop();
            return null;
        }
    }

    private static String generateError(String errorMessage, Representation format)
    {
        JSONObject response_raw = new JSONObject().put("error", errorMessage);
        if(format == Representation.XML)
            return XML.toString(response_raw);
        return response_raw.toString();
    }

    public static Route createPatient = (request, response) -> {
        Representation request_format = request.attribute("request_format");
        Representation reply_format = request.attribute("reply_format");
        LOGGER.debug("Request format: " + request_format + " Reply format: " + reply_format);

        try {
            JSONObject resource = parseResource(request.body(), request_format);
            response.body(new ConverterOpenempi().patientCreate(resource, reply_format));
            return response;
        }
        catch (DataFormatException e)
        {
            response.body(generateError("Invalid Value", reply_format));
            LOGGER.info("Invalid Parameter Received", e);
            return response;
        }
        catch (ClassCastException e)
        {
            response.body(generateError("Incompatible Type", reply_format));
            LOGGER.info("Incompatible Type Received", e);
            return response;
        }
    };

    public static Route searchPatientByGet = (request, response) -> {
        Representation format = request.attribute("format");
        LOGGER.debug("Body: " + request.body());
        response.body(new ConverterOpenempi().patientSearch(null, format));

        return response;
    };

    public static Route searchPatientByPost = (request, response) -> {
        Representation format = request.attribute("format");
        JSONObject body = new JSONObject(request.body());
        response.body(new ConverterOpenempi().patientSearch(body, format));

        return response;
    };

    public static Route readPatient = (request, response) -> {
        Representation format = request.attribute("format");
        response.body(new ConverterOpenempi().patientRead(request.params("id"), format));
        return response;
    };

    public static Route updatePatient = (request, response) -> {
        String id = request.params("id");
        Representation request_format = request.attribute("request_format");
        Representation reply_format = request.attribute("reply_format");
        LOGGER.debug("Request format: " + request_format + " Reply format: " + reply_format);

        try {
            JSONObject resource = parseResource(request.body(), request_format);
            response.body(new ConverterOpenempi().patientUpdate(id, resource, reply_format));
            return response;
        }
        catch (DataFormatException e)
        {
            response.body(generateError("Invalid Value", reply_format));
            LOGGER.info("Invalid Parameter Received", e);
            return response;
        }
        catch (ClassCastException e)
        {
            response.body(generateError("Incompatible Type", reply_format));
            LOGGER.info("Incompatible Type Received", e);
            return response;
        }
    };

    public static Route patchPatient = (request, response) -> {
        String id = request.params("id");
        Representation request_format = request.attribute("request_format");
        Representation reply_format = request.attribute("reply_format");
        LOGGER.debug("Request format: " + request_format + " Reply format: " + reply_format);

        try {
            JsonPatch patch = parsePatch(request.body(), request_format);
            response.body(new ConverterOpenempi().patientPatch(id, patch, reply_format));
            return response;
        }
        catch (IOException e)
        {
            response.body(generateError("Invalid Patch", reply_format));
            LOGGER.info("Invalid Patch Received", e);
            return response;
        }
    };

    public static Route deletePatient = (request, response) -> {
        Representation format = request.attribute("format");
        response.body(new ConverterOpenempi().patientDelete(request.params("id"), format));
        return response;
    };
}