package ru.nsu.fit.g16201.galieva.Filter.View;

import ru.nsu.fit.g16201.galieva.Filter.Model.Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class ImagePanel extends JPanel {
    private BufferedImage image;
    private Model model;
    private Graphics2D graphics2D;

    private static final Color borderColor = Color.black;

    private int imageAWidth, imageAHeight;
    private int fragmentSize = 350;
    private MouseAdapter selectAreaAdapter;

    public ImagePanel(Model _model, ImagePanelClickListener clickListener) {
        this.model = _model;

        if (clickListener == null) {
            return;
        }

        image = new BufferedImage(1100, 400, BufferedImage.TYPE_INT_ARGB);
        graphics2D = image.createGraphics();
        graphics2D.setColor(borderColor);
        graphics2D.setBackground(getBackground());

        setPreferredSize(new Dimension(1100, 400));

        drawRectangles();

    }

    private void drawRectangles() {
        float[] dash = {7};
        graphics2D.setStroke(new BasicStroke(1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 3, dash, 0.0f));
        graphics2D.drawLine(9, 9, 9, 360);
        graphics2D.drawLine(9, 9, 360, 9);
        graphics2D.drawLine(360, 9, 360, 360);
        graphics2D.drawLine(9, 360, 360, 360);

        graphics2D.drawLine(369, 9, 369, 360);
        graphics2D.drawLine(369, 9, 720, 9);
        graphics2D.drawLine(369, 360, 720, 360);
        graphics2D.drawLine(720, 9, 720, 360);

        graphics2D.drawLine(729, 9, 729, 360);
        graphics2D.drawLine(729, 9, 1080, 9);
        graphics2D.drawLine(729, 360, 1080, 360);
        graphics2D.drawLine(1080, 9, 1080, 360);
    }

    public void load() {
        graphics2D.clearRect(10, 10, 350, 350);
        graphics2D.clearRect(370, 10, 350, 350);
        graphics2D.clearRect(730, 10, 350, 350);

        int actualWidth = model.getImageA().getWidth();
        int actualHeight = model.getImageA().getHeight();
        float imageWidth = (actualWidth > 350) ? 350 : actualWidth;
        float imageHeight = (actualHeight > 350) ? 350 : actualHeight;
        if (actualWidth > 350 || actualHeight > 350) {
            if (actualWidth > actualHeight)
                imageHeight = (float)actualHeight/actualWidth*350;
            else imageWidth = (float)actualWidth/actualHeight*350;
        }
        imageAWidth = (int) imageWidth;
        imageAHeight = (int)imageHeight;

        graphics2D.drawImage(model.getImageA(), 10, 10, (int)imageWidth, (int)imageHeight, this);
        repaint();
    }

    public BufferedImage copyImage(BufferedImage sourceImage) {
        BufferedImage dst = new BufferedImage(sourceImage.getWidth(), sourceImage.getHeight(), sourceImage.getType());
        dst.getGraphics().drawImage(sourceImage, 0, 0, sourceImage.getWidth(), sourceImage.getHeight(), null);
        return dst;
    }

    public void setSelectMode(boolean enabled) {
        if (model.getImageA() != null) {
            if (enabled) {
                selectAreaAdapter = new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        super.mousePressed(e);
                        if (e.getX() > 10 && e.getX() < imageAWidth && e.getY() > 10 && e.getY() < imageAHeight)
                            mouseDragged(e);
                    }

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        super.mouseReleased(e);
                        if (e.getX() > 10 && e.getX() < imageAWidth && e.getY() > 10 && e.getY() < imageAHeight)
                            mouseDragged(e);
                    }

                    @Override
                    public void mouseDragged(MouseEvent e) {
                        super.mouseDragged(e);
                        int x = e.getX();
                        int y = e.getY();

                        int actualWidth = model.getImageA().getWidth();
                        int actualHeight = model.getImageA().getHeight();

                        float areaWidth = 0.0f, areaHeight = 0.0f;
                        if (actualWidth > fragmentSize && actualHeight > fragmentSize) {
                            areaWidth = (fragmentSize*fragmentSize)/actualWidth;
                            areaHeight = areaWidth;
                        } else if (actualWidth > fragmentSize && actualHeight <= fragmentSize) {
                            areaWidth = (fragmentSize*fragmentSize)/areaWidth;
                            areaHeight = actualHeight*fragmentSize/actualWidth;
                        } else if (actualWidth <= fragmentSize && actualHeight > fragmentSize) {
                            areaWidth = actualWidth*fragmentSize/actualHeight;
                            areaHeight = (fragmentSize*fragmentSize)/actualHeight;
                        } else if (areaWidth <= fragmentSize && areaHeight <= fragmentSize) {
                            areaWidth = actualWidth;
                            areaHeight = actualHeight;
                        }

                        float halfAreaWidth = areaWidth/2;
                        float halfAreaHeight = areaHeight/2;

                        float leftBound = halfAreaWidth + 10;
                        float rightBound = imageAWidth - halfAreaWidth + 10;
                        float upperBound = halfAreaHeight + 10;
                        float bottomBound = imageAHeight - halfAreaHeight + 10;

                        graphics2D.drawImage(model.getImageA(), 10, 10, imageAWidth, imageAHeight, e.getComponent());

                        if (x > leftBound && x < rightBound && y > upperBound && y < bottomBound) {
                            selectFragment(new Point((int)(x - halfAreaWidth), (int)(y - halfAreaHeight)), new Point((int)(x + halfAreaWidth), (int)(y + halfAreaHeight)), e);
                        } else if (x < leftBound && y > upperBound && y < bottomBound) {
                            selectFragment(new Point(10,(int)(y - halfAreaHeight)), new Point((int)(areaWidth + 10), (int)(y + halfAreaHeight)), e);
                        } else if (x < leftBound && y < upperBound) {
                            selectFragment(new Point(10, 10), new Point((int)(areaWidth + 10), (int)(areaHeight + 10)), e);
                        } else if (x > leftBound && x < rightBound && y < upperBound) {
                            selectFragment(new Point((int)(x - halfAreaWidth), 10), new Point((int)(x + halfAreaWidth), (int)(areaHeight + 10)), e);
                        } else if (x > rightBound && y < upperBound) {
                            selectFragment(new Point((int)(imageAWidth - areaWidth + 10), 10), new Point(imageAWidth+10, (int)(areaHeight + 10)), e);
                        } else if (x > rightBound && y > upperBound && y < bottomBound) {
                            selectFragment(new Point((int)(imageAWidth - areaWidth + 10), (int)(y - halfAreaHeight)), new Point(imageAWidth + 10, (int)(y + halfAreaHeight)), e);
                        } else if (x > rightBound && y > bottomBound) {
                            selectFragment(new Point((int)(imageAWidth - areaWidth + 10), (int)(imageAHeight - areaHeight + 10)), new Point(imageAWidth+10, imageAHeight+10), e);
                        } else if (x > leftBound && x < rightBound && y > bottomBound) {
                            selectFragment(new Point((int)(x - halfAreaWidth), (int)(imageAHeight - areaHeight + 10)), new Point((int)(x + halfAreaWidth), imageAHeight+10), e);
                        } else if (x < leftBound && y > bottomBound) {
                            selectFragment(new Point(10, (int)(imageAHeight - areaHeight + 10)), new Point((int)(areaWidth+10), imageAHeight+10), e);
                        }

                        repaint();
                    }

                    private void selectFragment(Point p1, Point p2, MouseEvent e) {
                        graphics2D.setXORMode(Color.white);
                        graphics2D.drawLine(p1.x, p1.y, p1.x, p2.y - 1);
                        graphics2D.drawLine(p1.x, p1.y, p2.x - 1, p1.y);
                        graphics2D.drawLine(p2.x - 1, p2.y - 1, p1.x, p2.y - 1);
                        graphics2D.drawLine(p2.x - 1, p2.y - 1, p2.x - 1, p1.y);
                        graphics2D.setPaintMode();

                        int actualWidth = model.getImageA().getWidth();
                        int actualHeight = model.getImageA().getHeight();
                        float decompressionX = (imageAWidth > actualWidth) ? (float)imageAWidth/actualWidth : (float)actualWidth/imageAWidth;
                        float decompressionY = (imageAHeight > actualHeight) ? (float)imageAHeight/actualHeight : (float)actualHeight/imageAHeight;
                        decompressionX = (decompressionX < 0.999999) ? 1 : decompressionX;
                        decompressionY = (decompressionY < 0.999999) ? 1 : decompressionY;

                        BufferedImage subimage = model.getImageA().getSubimage((int)(decompressionX*(p1.x-10)), (int)(decompressionY*(p1.y-10)), (int)(decompressionX*(p2.x-p1.x)), (int)(decompressionY*(p2.y-p1.y)));
                        model.setImageB(copyImage(subimage));
                        graphics2D.clearRect(370, 10, 350, 350);
                        graphics2D.drawImage(model.getImageB(), 370, 10, actualWidth < 350 ? (int)(decompressionX*(p2.x-p1.x)):350, actualHeight < 350 ? (int)(decompressionY*(p2.y-p1.y)):350, e.getComponent());
                    }
                };
                addMouseListener(selectAreaAdapter);
                addMouseMotionListener(selectAreaAdapter);
            } else {
                removeMouseListener(selectAreaAdapter);
                removeMouseMotionListener(selectAreaAdapter);
                graphics2D.drawImage(model.getImageA(), 10, 10, imageAWidth, imageAHeight, this);
                repaint();
            }
        }
    }

    public void copyBtoC() {
        if (model.getImageB() != null) {
            model.setImageC(copyImage(model.getImageB()));
            graphics2D.drawImage(model.getImageC(), 730, 10, 350, 350, this);
            repaint();
        }
    }

    public void copyCtoB() {
        if (model.getImageC() != null) {
            model.setImageB(copyImage(model.getImageC()));
            graphics2D.drawImage(model.getImageB(), 370, 10, 350, 350, this);
            repaint();
        }
    }

    public void discolorFilter() {
        if (model.getImageB() != null) {
            model.applyDiscolorFilter();
            graphics2D.drawImage(model.getImageC(), 730, 10, 350, 350, this);
            repaint();
        }
    }

    public void negativeFilter() {
        if (model.getImageB() != null) {
            model.applyNegativeFilter();
            graphics2D.drawImage(model.getImageC(), 730, 10, 350, 350, this);
            repaint();
        }
    }

    public void orderedDithering(int countR, int countG, int countB) {
        if (model.getImageB() != null) {
            model.applyOrderedDithering(countR, countG, countB);
            graphics2D.drawImage(model.getImageC(), 730, 10, 350, 350, this);
            repaint();
        }
    }

    public void floydSteinbergDithering(int countR, int countG, int countB) {
        if (model.getImageB() != null) {
            model.applyFloydSteinbergDithering(countR, countG, countB);
            graphics2D.drawImage(model.getImageC(), 730, 10, 350, 350, this);
            repaint();
        }
    }

    public void robertsOperator(int C) {
        if (model.getImageB() != null) {
            model.applyRobertsOperator(C);
            graphics2D.drawImage(model.getImageC(), 730, 10, 350, 350, this);
            repaint();
        }
    }

    public void sobelOperator(int C) {
        if (model.getImageB() != null) {
            model.applySobelOperator(C);
            graphics2D.drawImage(model.getImageC(), 730, 10, 350, 350, this);
            repaint();
        }
    }

    public void zoomFilter() {
        if (model.getImageB() != null) {
            model.applyZoom();
            graphics2D.drawImage(model.getImageC(), 730, 10, 350, 350, this);
            repaint();
        }
    }

    public void rotationFilter(int angle) {
        if (model.getImageB() != null) {
            model.applyRotation(angle);
            graphics2D.drawImage(model.getImageC(), 730, 10, 350, 350, this);
            repaint();
        }
    }

    public void sharpening() {
        if (model.getImageB() != null) {
            model.applySharpening(model.getImageB());
            graphics2D.drawImage(model.getImageC(), 730, 10, 350, 350, this);
            repaint();
        }
    }

    public void stamping() {
        if (model.getImageB() != null) {
            model.applyStamping();
            graphics2D.drawImage(model.getImageC(), 730, 10, 350, 350, this);
            repaint();
        }
    }

    public void medianFilter() {
        if (model.getImageB() != null) {
            model.applyMedianFilter();
            graphics2D.drawImage(model.getImageC(), 730, 10, 350, 350, this);
            repaint();
        }
    }

    public void aquarelFilter() {
        if (model.getImageB() != null) {
            model.applyAquarelFilter();
            graphics2D.drawImage(model.getImageC(), 730, 10, 350, 350, this);
            repaint();
        }
    }

    public void blurFilter() {
        if (model.getImageB() != null) {
            model.applyBlurFilter();
            graphics2D.drawImage(model.getImageC(), 730, 10, 350, 350, this);
            repaint();
        }
    }

    public void gammaCorrection(int gamma) {
        if (model.getImageB() != null) {
            model.applyGammaCorrection(gamma);
            graphics2D.drawImage(model.getImageC(), 730, 10, 350, 350, this);
            repaint();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, 1100, 400, this);
    }
}
