package Helperclass;

/**
 * Created by GJ on 25/02/2015.
 */

/**
 * class to help with http codes
 */
public enum HTTPStatusCode {

    OK {
        @Override
        public String toString() {
            return "200 OK";
        }

        @Override
        public String getResponse(String httpVer) {
            return "HTTP/" + httpVer + " 200 OK";
        }
    },
    NOT_FOUND {
        @Override
        public String toString() {
            return "404 Not Found";
        }

        @Override
        public String getResponse(String httpVer) {
            return "HTTP/" + httpVer + "404 NOT_FOUND";
        }
    },
    BAD_REQUEST {
        @Override
        public String toString() {
            return "400: Bad Request";
        }

        @Override
        public String getResponse(String httpVer) {
            return "HTTP/" + httpVer + " 400 Bad request";
        }
    },
    SERVER_ERROR {
        @Override
        public String toString() {
            return "500 Server Error";
        }

        @Override
        public String getResponse(String httpVer) {
            return "HTTP/" + httpVer + " 500 SERVER_ERROR";
        }
    },
    NOT_MODIFIED {
        @Override
        public String toString() {
            return "304 Not Modified";
        }

        @Override
        public String getResponse(String httpVer) {
            return "HTTP/" + httpVer + "304 NOT_MODIFIED";
        }
    };

    public abstract String toString();

    public abstract String getResponse(String httpVer);


}
