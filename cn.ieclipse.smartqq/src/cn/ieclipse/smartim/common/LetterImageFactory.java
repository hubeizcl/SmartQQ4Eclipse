package cn.ieclipse.smartim.common;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

public class LetterImageFactory {
    
    public static Image create(char letter, int color, int shape, int bg) {
        if (Display.getCurrent() != null) {
            Product p = new Product(letter, color, shape, bg);
            Image image = new Image(Display.getCurrent(), p.getImageData());
            return image;
        }
        return null;
    }
    
    public static Image createCircle(char letter, int color, int bg) {
        return create(letter, color, Product.SHAPE_CIRCLE, bg);
    }
    
    public static Image createCircle(char letter, int color) {
        return create(letter, color, Product.SHAPE_CIRCLE,
                SWT.COLOR_TRANSPARENT);
    }
    
    public static Image create(char letter, int color, int bg) {
        return create(letter, color, Product.SHAPE_RECTANGE, bg);
    }
    
    public static Image create(char letter, int color) {
        return create(letter, color, Product.SHAPE_RECTANGE,
                SWT.COLOR_TRANSPARENT);
    }
    
    public static ImageDescriptor createDescriptor(char letter, int color,
            int shape, int bg) {
        return new Product(letter, color, shape, bg);
    }
    
    public static ImageDescriptor createDescriptor(char letter, int color,
            int bg) {
        return new Product(letter, color, Product.SHAPE_RECTANGE, bg);
    }
    
    public static ImageDescriptor createDescriptor(char letter, int color) {
        return new Product(letter, color);
    }
    
    public static ImageDescriptor createCircleDescriptor(char letter, int color,
            int bg) {
        return new Product(letter, color, Product.SHAPE_CIRCLE, bg);
    }
    
    public static ImageDescriptor createCircleDescriptor(char letter,
            int color) {
        return new Product(letter, color, Product.SHAPE_CIRCLE);
    }
    
    public static class Product extends ImageDescriptor {
        private final char mLetter;
        private final int mBgColor;
        private final int mColor;
        private final int mShape;
        private final int mWidth = 16;
        private final int mHeight = 16;
        private final int mRadius = 4;
        private final int mBorderWidth = 1;
        public static final int SHAPE_CIRCLE = 0;
        public static final int SHAPE_RECTANGE = 1;
        
        public Product(char letter, int color, int shape, int bgColor) {
            this.mLetter = Character.toUpperCase(letter);
            this.mColor = color;
            this.mShape = shape;
            this.mBgColor = bgColor;
        }
        
        public Product(char letter, int color, int shape) {
            this(letter, color, shape, SWT.COLOR_TRANSPARENT);
        }
        
        public Product(char letter, int color) {
            this(letter, color, SHAPE_RECTANGE);
        }
        
        public ImageData getImageData() {
            Display display = Display.getCurrent();
            if (display == null) {
                return null;
            }
            
            Image image = new Image(display, mWidth, mHeight);
            
            GC gc = new GC(image);
            gc.setAdvanced(true);
            gc.setAntialias(SWT.ON);
            gc.setTextAntialias(SWT.ON);
            
            int w = mWidth - mBorderWidth;
            int h = mHeight - mBorderWidth;
            
            Color bgColor = display.getSystemColor(this.mBgColor);
            
            gc.setBackground(bgColor);
            gc.fillRectangle(0, 0, w, h);
            
            // gc.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
            // if (this.mShape == SHAPE_CIRCLE) {
            // gc.fillOval(0, 0, w, h);
            // }
            // else if (this.mShape == SHAPE_RECTANGE) {
            // gc.fillRoundRectangle(0, 0, w, h, mRadius, mRadius);
            // }
            
            gc.setForeground(display.getSystemColor(mColor));
            gc.setLineWidth(mBorderWidth);
            if (this.mShape == SHAPE_CIRCLE) {
                gc.drawOval(0, 0, w, h);
            }
            else if (this.mShape == SHAPE_RECTANGE) {
                gc.drawRoundRectangle(0, 0, w, h, mRadius, mRadius);
            }
            
            Font font = display.getSystemFont();
            FontData[] fds = font.getFontData();
            fds[0].setStyle(SWT.BOLD);
            
            fds[0].setHeight((int) (864.0D / display.getDPI().y));
            
            font = new Font(display, fds);
            gc.setFont(font);
            gc.setForeground(display.getSystemColor(this.mColor));
            
            int ofx = 1;
            int ofy = -1;
            if (this.mLetter == 'A' || this.mLetter == 'S') {
                ofx = 2;
            }
            
            String s = Character.toString(this.mLetter);
            Point p = gc.textExtent(s);
            int tx = (mWidth + ofx - p.x) / 2;
            int ty = (mHeight + ofy - p.y) / 2;
            gc.drawText(s, tx, ty, true);
            
            font.dispose();
            gc.dispose();
            
            ImageData data = image.getImageData();
            image.dispose();
            int backgroundPixel = data.palette.getPixel(bgColor.getRGB());
            data.transparentPixel = backgroundPixel;
            bgColor.dispose();
            return data;
        }
    }
}
