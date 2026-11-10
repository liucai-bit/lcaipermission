package com.liucai.image;

import android.graphics.Path;

public class LcaiShapePathFactory {


    /**
     * 在 (ox, oy, w, h) 区域内构建形状路径。
     * 形状在区域中心、按短边自动缩放，保证比例正确。
     */
    public static void build(Path path, LcaiShape shape, float ox, float oy, float w, float h) {
        path.reset();
        if (shape == null || shape == LcaiShape.NONE) {
            path.addRect(ox, oy, ox + w, oy + h, Path.Direction.CW);
            return;
        }
        float size = Math.min(w, h);
        float cx = ox + w / 2f;
        float cy = oy + h / 2f;
        float R = size / 2f;

        switch (shape) {
            case CIRCLE:
                path.addCircle(cx, cy, R * 0.98f, Path.Direction.CW);
                break;

            case ROUNDED: {
                float left = ox + (w - size) / 2f;
                float top  = oy + (h - size) / 2f;
                float radius = size * 0.16f;
                path.addRoundRect(left, top, left + size, top + size,
                        radius, radius, Path.Direction.CW);
                break;
            }

            case HEART: {
                int N = 240;
                float scale = (size * 0.95f) / 32f;
                for (int i = 0; i <= N; i++) {
                    double t = (i / (double) N) * Math.PI * 2;
                    double x = 16 * Math.pow(Math.sin(t), 3);
                    double y = 13 * Math.cos(t) - 5 * Math.cos(2 * t)
                            - 2 * Math.cos(3 * t) - Math.cos(4 * t);
                    float px = cx + (float) x * scale;
                    float py = cy + (float) (-y - 2.75) * scale;
                    if (i == 0) path.moveTo(px, py); else path.lineTo(px, py);
                }
                path.close();
                break;
            }

            case STAR5:
                buildStar(path, cx, cy, R * 0.98f, 5, 0.382f);
                break;

            case STAR6:
                buildStar(path, cx, cy, R * 0.98f, 6, 0.577f);
                break;

            case DIAMOND:
                buildPolygon(path, cx, cy, R * 0.98f, 4, (float) -Math.PI / 2);
                break;

            case HEXAGON:
                buildPolygon(path, cx, cy, R * 0.98f, 6, (float) -Math.PI / 2);
                break;

            case FLOWER: {
                int N = 300;
                float r0 = R * 0.95f;
                for (int i = 0; i <= N; i++) {
                    double theta = (i / (double) N) * Math.PI * 2;
                    double r = r0 * (0.68 + 0.32 * Math.abs(Math.sin(2.5 * theta)));
                    float px = cx + (float) (Math.cos(theta - Math.PI / 2) * r);
                    float py = cy + (float) (Math.sin(theta - Math.PI / 2) * r);
                    if (i == 0) path.moveTo(px, py); else path.lineTo(px, py);
                }
                path.close();
                break;
            }
        }
    }

    private static void buildStar(Path path, float cx, float cy,
                                  float R, int points, float innerRatio) {
        double step = Math.PI / points;
        for (int i = 0; i < points * 2; i++) {
            double r = (i % 2 == 0) ? R : R * innerRatio;
            double a = -Math.PI / 2 + i * step;
            float x = cx + (float) (Math.cos(a) * r);
            float y = cy + (float) (Math.sin(a) * r);
            if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
        }
        path.close();
    }

    private static void buildPolygon(Path path, float cx, float cy,
                                     float R, int n, float startAngle) {
        for (int i = 0; i < n; i++) {
            double a = startAngle + (i * Math.PI * 2) / n;
            float x = cx + (float) (Math.cos(a) * R);
            float y = cy + (float) (Math.sin(a) * R);
            if (i == 0) path.moveTo(x, y); else path.lineTo(x, y);
        }
        path.close();
    }
}