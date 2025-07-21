package com.company.MultiModule.models;

public class EBook extends Book {
    private final double fileSizeMB;
    private final String format;
    private final String downloadLink;

    private EBook(EBookBuilder builder) {
        super(builder);
        this.fileSizeMB = builder.fileSizeMB;
        this.format = builder.format;
        this.downloadLink = builder.downloadLink;
    }

    public double getFileSizeMB() { return fileSizeMB; }
    public String getFormat() { return format; }
    public String getDownloadLink() { return downloadLink; }

    @Override
    public String toString() {
        return super.toString() +
                String.format(" [EBook: %.2fMB, format=%s, link=%s]",
                        fileSizeMB, format, downloadLink);
    }

    public static class EBookBuilder extends Book.Builder<EBookBuilder> {
        private double fileSizeMB;
        private String format;
        private String downloadLink;

        public EBookBuilder fileSizeMB(double fileSizeMB) {
            this.fileSizeMB = fileSizeMB;
            return self();
        }

        public EBookBuilder format(String format) {
            this.format = format;
            return self();
        }

        public EBookBuilder downloadLink(String downloadLink) {
            this.downloadLink = downloadLink;
            return self();
        }

        @Override
        protected EBookBuilder self() {
            return this;
        }

        @Override
        public EBook build() {
            return new EBook(this);
        }
    }
}