using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Forest
{
    internal enum ForestIcon
    {
        Today, Forest, Album, Settings, Plus, Close, Check, ChevronRight, ChevronLeft,
        Leaf, Heart, Gift, Compass, Flower, Book, Mail, Moon, Sun, Trash, Repeat, ArrowRight, Pause
    }

    [DisallowMultipleComponent]
    [RequireComponent(typeof(CanvasRenderer))]
    public sealed class ForestIconGraphic : MaskableGraphic
    {
        [SerializeField] private ForestIcon _icon;
        [SerializeField, Range(.8f, 3)] private float _stroke = 1.65f;
        private VertexHelper _mesh;
        private Rect _rect;
        private float _scale;
        private Vector2 _origin;

        internal ForestIcon Icon { get => _icon; set { if (_icon == value) return; _icon = value; SetVerticesDirty(); } }

        protected override void OnPopulateMesh(VertexHelper mesh)
        {
            mesh.Clear();
            _mesh = mesh;
            _rect = GetPixelAdjustedRect();
            _scale = Mathf.Min(_rect.width, _rect.height) / 24;
            _origin = new Vector2(_rect.xMin + (_rect.width - _scale * 24) * .5f, _rect.yMax - (_rect.height - _scale * 24) * .5f);
            if (_scale <= 0) return;
            switch (_icon)
            {
                case ForestIcon.Today:
                    RoundedBox(4, 5, 16, 16, 2.6f); Line(4, 10, 20, 10); Line(8, 3, 8, 7); Line(16, 3, 16, 7);
                    Line(8, 15, 10.5f, 17.5f); Line(10.5f, 17.5f, 16, 13);
                    break;
                case ForestIcon.Forest:
                    Curve(12, 3, 9, 2, 6, 5, 7, 8); Curve(7, 8, 2, 9, 3, 15, 7, 16);
                    Curve(7, 16, 9, 18, 15, 18, 17, 16); Curve(17, 16, 22, 15, 22, 9, 17, 8);
                    Curve(17, 8, 18, 5, 15, 2, 12, 3); Line(12, 11, 12, 22); Line(12, 15, 8.5f, 12.5f); Line(12, 17, 15.5f, 13.5f);
                    break;
                case ForestIcon.Album:
                    RoundedBox(5, 3, 15, 17, 2.4f); Line(3, 7, 3, 21); Line(3, 21, 16, 21);
                    Circle(10, 8, 1.3f); Path(7.5f, 16, 11, 12.5f, 13.2f, 14.5f, 15.8f, 11, 18, 14);
                    break;
                case ForestIcon.Settings:
                    Gear(); Circle(12, 12, 3.1f); break;
                case ForestIcon.Plus: Line(5, 12, 19, 12); Line(12, 5, 12, 19); break;
                case ForestIcon.Close: Line(6, 6, 18, 18); Line(6, 18, 18, 6); break;
                case ForestIcon.Check: Path(4.5f, 12, 9.5f, 17, 19.5f, 6.5f); break;
                case ForestIcon.ChevronRight: Path(9, 5, 16, 12, 9, 19); break;
                case ForestIcon.ChevronLeft: Path(15, 5, 8, 12, 15, 19); break;
                case ForestIcon.Leaf:
                    Curve(4.5f, 18.5f, 0, 7, 11, 4, 20, 3.5f); Curve(20, 3.5f, 21, 16, 15, 22, 4.5f, 18.5f);
                    Curve(3, 21, 7, 15, 12, 12, 17, 7); Line(7.5f, 15.8f, 7.5f, 11.7f); Line(11, 12, 15, 12);
                    break;
                case ForestIcon.Heart:
                    Curve(12, 20, 6, 16, 2, 12, 3, 7.5f); Curve(3, 7.5f, 4, 2, 10, 2, 12, 6.5f);
                    Curve(12, 6.5f, 14, 2, 20, 2, 21, 7.5f); Curve(21, 7.5f, 22, 12, 18, 16, 12, 20);
                    break;
                case ForestIcon.Gift:
                    RoundedBox(3, 8, 18, 4, 1); Path(5, 12, 5, 21, 19, 21, 19, 12); Line(12, 8, 12, 21);
                    Curve(12, 8, 9, 0, 3, 2, 6, 6); Curve(6, 6, 7, 8, 10, 8, 12, 8);
                    Curve(12, 8, 15, 0, 21, 2, 18, 6); Curve(18, 6, 17, 8, 14, 8, 12, 8);
                    break;
                case ForestIcon.Compass:
                    Circle(12, 12, 9); Path(15.8f, 8.2f, 13.5f, 13.5f, 8.2f, 15.8f, 10.5f, 10.5f, 15.8f, 8.2f); Line(10.5f, 10.5f, 13.5f, 13.5f);
                    break;
                case ForestIcon.Flower:
                    for (int i = 0; i < 5; i++)
                    {
                        float angle = (-90 + i * 72) * Mathf.Deg2Rad;
                        Circle(12 + Mathf.Cos(angle) * 5.1f, 10.5f + Mathf.Sin(angle) * 5.1f, 3.35f);
                    }
                    Circle(12, 10.5f, 2.2f); Line(12, 16.6f, 12, 22); Curve(12, 21, 16, 21, 18, 18, 17.5f, 17);
                    break;
                case ForestIcon.Book:
                    Curve(12, 5, 9, 2, 5, 3, 3, 4); Path(3, 4, 3, 19);
                    Curve(3, 19, 7, 17.5f, 10, 18.5f, 12, 21); Curve(12, 21, 14, 18.5f, 17, 17.5f, 21, 19);
                    Line(21, 19, 21, 4); Curve(21, 4, 19, 3, 15, 2, 12, 5); Line(12, 5, 12, 21);
                    break;
                case ForestIcon.Mail:
                    RoundedBox(2.8f, 5, 18.4f, 14, 2.2f); Path(4, 7, 12, 13.3f, 20, 7); Line(3.5f, 17, 8.5f, 12.4f); Line(20.5f, 17, 15.5f, 12.4f);
                    break;
                case ForestIcon.Moon:
                    Curve(15, 3, 6, 0, 0, 10, 5.5f, 17); Curve(5.5f, 17, 10, 23, 19, 21, 21, 14);
                    Curve(21, 14, 12, 17, 8, 8, 15, 3); break;
                case ForestIcon.Sun:
                    Circle(12, 12, 4.2f);
                    for (int i = 0; i < 8; i++) { float a = i * Mathf.PI / 4; Line(12 + Mathf.Cos(a) * 7.3f, 12 + Mathf.Sin(a) * 7.3f, 12 + Mathf.Cos(a) * 9.8f, 12 + Mathf.Sin(a) * 9.8f); }
                    break;
                case ForestIcon.Trash:
                    Line(3.5f, 6, 20.5f, 6); Path(9, 6, 9, 3, 15, 3, 15, 6); Path(6, 6, 7, 21, 17, 21, 18, 6); Line(10, 10, 10.5f, 17); Line(14, 10, 13.5f, 17); break;
                case ForestIcon.Repeat:
                    Path(16, 3, 20, 7, 16, 11); Curve(20, 7, 9, 1, 2, 6, 3.5f, 12);
                    Path(8, 21, 4, 17, 8, 13); Curve(4, 17, 15, 23, 22, 18, 20.5f, 12); break;
                case ForestIcon.ArrowRight: Line(3, 12, 21, 12); Path(14, 5, 21, 12, 14, 19); break;
                case ForestIcon.Pause: Line(8, 5, 8, 19); Line(16, 5, 16, 19); break;
            }
        }

        private Vector2 Point(float x, float y) { return _origin + new Vector2(x * _scale, -y * _scale); }
        private void Path(params float[] points)
        {
            for (int i = 0; i + 3 < points.Length; i += 2) Line(points[i], points[i + 1], points[i + 2], points[i + 3]);
        }

        private void Line(float x1, float y1, float x2, float y2)
        {
            var a = Point(x1, y1); var b = Point(x2, y2);
            var direction = b - a;
            if (direction.sqrMagnitude < .00001f) return;
            var normal = new Vector2(-direction.y, direction.x).normalized * (_stroke * _scale * .5f);
            int start = _mesh.currentVertCount;
            _mesh.AddVert(a - normal, color, Vector2.zero); _mesh.AddVert(a + normal, color, Vector2.zero);
            _mesh.AddVert(b + normal, color, Vector2.zero); _mesh.AddVert(b - normal, color, Vector2.zero);
            _mesh.AddTriangle(start, start + 1, start + 2); _mesh.AddTriangle(start, start + 2, start + 3);
            Dot(a, _stroke * _scale * .5f); Dot(b, _stroke * _scale * .5f);
        }

        private void Dot(Vector2 point, float radius)
        {
            int start = _mesh.currentVertCount;
            _mesh.AddVert(point, color, Vector2.zero);
            const int steps = 8;
            for (int i = 0; i < steps; i++)
            {
                float angle = i * Mathf.PI * 2 / steps;
                _mesh.AddVert(point + new Vector2(Mathf.Cos(angle), Mathf.Sin(angle)) * radius, color, Vector2.zero);
            }
            for (int i = 0; i < steps; i++) _mesh.AddTriangle(start, start + 1 + i, start + 1 + (i + 1) % steps);
        }

        private void Curve(float ax, float ay, float bx, float by, float cx, float cy, float dx, float dy)
        {
            var a = new Vector2(ax, ay); var b = new Vector2(bx, by); var c = new Vector2(cx, cy); var d = new Vector2(dx, dy);
            var previous = a;
            const int steps = 12;
            for (int i = 1; i <= steps; i++)
            {
                float t = i / (float)steps, u = 1 - t;
                var point = u * u * u * a + 3 * u * u * t * b + 3 * u * t * t * c + t * t * t * d;
                Line(previous.x, previous.y, point.x, point.y);
                previous = point;
            }
        }

        private void Circle(float x, float y, float radius)
        {
            const int steps = 32;
            for (int i = 0; i < steps; i++)
            {
                float a = i * Mathf.PI * 2 / steps, b = (i + 1) * Mathf.PI * 2 / steps;
                Line(x + Mathf.Cos(a) * radius, y + Mathf.Sin(a) * radius, x + Mathf.Cos(b) * radius, y + Mathf.Sin(b) * radius);
            }
        }

        private void RoundedBox(float x, float y, float width, float height, float radius)
        {
            const float k = .5522848f;
            float right = x + width, bottom = y + height;
            Line(x + radius, y, right - radius, y);
            Curve(right - radius, y, right - radius + radius * k, y, right, y + radius - radius * k, right, y + radius);
            Line(right, y + radius, right, bottom - radius);
            Curve(right, bottom - radius, right, bottom - radius + radius * k, right - radius + radius * k, bottom, right - radius, bottom);
            Line(right - radius, bottom, x + radius, bottom);
            Curve(x + radius, bottom, x + radius - radius * k, bottom, x, bottom - radius + radius * k, x, bottom - radius);
            Line(x, bottom - radius, x, y + radius);
            Curve(x, y + radius, x, y + radius - radius * k, x + radius - radius * k, y, x + radius, y);
        }

        private void Gear()
        {
            Vector2 first = Vector2.zero, previous = Vector2.zero;
            for (int i = 0; i < 32; i++)
            {
                float angle = (i * 11.25f - 5.625f) * Mathf.Deg2Rad;
                float radius = i % 4 == 0 || i % 4 == 1 ? 9 : 7;
                var point = new Vector2(12 + Mathf.Cos(angle) * radius, 12 + Mathf.Sin(angle) * radius);
                if (i == 0) first = point; else Line(previous.x, previous.y, point.x, point.y);
                previous = point;
            }
            Line(previous.x, previous.y, first.x, first.y);
        }
    }
}
