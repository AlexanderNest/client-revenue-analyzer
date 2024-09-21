package ru.nesterov.session;

public enum UserDataCheck {
    FIRST_DATE_MISSING {
        @Override
        public boolean validate(UserData userData) {
            return userData.getFirstDate() == null;
        }

        @Override
        public String getMessage() {
            return "";
        }
    },
    SECOND_DATE_MISSING {
        @Override
        public boolean validate(UserData userData) {
            return userData.getSecondDate() == null;
        }

        @Override
        public String getMessage() {
            return "";
        }
    };

    public abstract boolean validate(UserData userData);
    public abstract String getMessage();
}
