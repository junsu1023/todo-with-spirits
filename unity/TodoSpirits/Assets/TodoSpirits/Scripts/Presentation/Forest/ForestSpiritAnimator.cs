using System.Collections.Generic;
using TodoSpirits.Core;
using UnityEngine;
using UnityEngine.UI;

namespace TodoSpirits.Presentation.Forest
{
    /// <summary>Existing expressions with authored body gestures; no extra sprite frames are fabricated.</summary>
    [RequireComponent(typeof(Image))]
    public sealed class ForestSpiritAnimator : MonoBehaviour
    {
        public Sprite[] Poses;
        public Sprite ReadingPose;
        public bool ReduceMotion;
        public string Activity = "idle";
        public string Motion => _motion;

        private Image _image;
        private RectTransform _rect, _shadow, _prop;
        private CanvasGroup _shadowGroup;
        private Vector2 _origin, _propOrigin, _shadowOrigin;
        private Vector3 _baseScale, _shadowScale;
        private Quaternion _baseRotation, _propRotation;
        private float _shadowAlpha = 1, _clock, _activityElapsed, _actionElapsed, _lastPerform = -10;
        private string _motion = "idle", _lastActivity;
        private Frame _current = Frame.Neutral;
        private enum GestureStyle { Balanced, Observant, Sociable }
        private GestureStyle _gestureStyle;
        private float _tempo = 1, _movement = 1;

        private struct Frame
        {
            public float At, X, Y, Roll, Width, Height, Prop;
            public int Pose;
            public static Frame Neutral => new Frame(0);
            public Frame(float at, float x = 0, float y = 0, float roll = 0, float width = 1, float height = 1, int pose = 0, float prop = 0)
            { At = at; X = x; Y = y; Roll = roll; Width = width; Height = height; Pose = pose; Prop = prop; }
            public static Frame Blend(Frame a, Frame b, float t)
            {
                return new Frame(0, Mathf.Lerp(a.X, b.X, t), Mathf.Lerp(a.Y, b.Y, t), Mathf.Lerp(a.Roll, b.Roll, t),
                    Mathf.Lerp(a.Width, b.Width, t), Mathf.Lerp(a.Height, b.Height, t), b.Pose, Mathf.Lerp(a.Prop, b.Prop, t));
            }
        }

        // Each gesture has anticipation, one readable action and a quiet settling period.
        private static readonly Frame[] Greeting = {
            new Frame(0), new Frame(.12f, y:-2, width:1.018f, height:.976f),
            new Frame(.32f, y:5, roll:-5, pose:2), new Frame(.57f, y:3, roll:5, pose:2),
            new Frame(.81f, y:2, roll:-3, pose:2), new Frame(1.12f, pose:2), new Frame(1.55f)
        };
        private static readonly Frame[] Joy = {
            new Frame(0), new Frame(.14f, y:-3, width:1.03f, height:.965f),
            new Frame(.36f, y:15, width:.985f, height:1.025f, pose:2),
            new Frame(.63f, y:-1, width:1.024f, height:.972f, pose:2),
            new Frame(.86f, y:2, pose:2), new Frame(1.15f, pose:2), new Frame(1.65f)
        };
        private static readonly Frame[] Curiosity = {
            new Frame(0), new Frame(.23f, x:2, y:1, roll:-7, pose:3),
            new Frame(.90f, x:2, y:1, roll:-7, pose:3), new Frame(1.32f, roll:2, pose:3), new Frame(1.8f)
        };
        private static readonly Frame[] Observation = {
            new Frame(0), new Frame(.28f, x:1, roll:-3.5f, pose:3),
            new Frame(.95f, x:1, roll:-3.5f, pose:3), new Frame(1.4f, roll:1, pose:3), new Frame(1.8f)
        };
        private static readonly Frame[] QuietJoy = {
            new Frame(0), new Frame(.2f, y:-1, width:1.012f, height:.989f),
            new Frame(.48f, y:5, roll:-2, pose:2), new Frame(.9f, y:1, roll:2, pose:2),
            new Frame(1.3f, pose:2), new Frame(1.8f)
        };
        private static readonly Frame[] SocialGreeting = {
            new Frame(0), new Frame(.12f, y:-1, width:1.014f, height:.984f),
            new Frame(.32f, y:6, roll:-4, pose:2), new Frame(.62f, y:3, roll:3, pose:2),
            new Frame(.98f, y:1, pose:2), new Frame(1.42f)
        };
        private static readonly Frame[] Reading = {
            new Frame(0), new Frame(.65f, y:-1, roll:3), new Frame(2.45f, y:-1, roll:3),
            new Frame(2.85f, x:2, roll:-2, pose:3, prop:1), new Frame(3.3f, y:-1, roll:3),
            new Frame(5.25f, y:-1, roll:3), new Frame(5.7f, y:1, pose:3), new Frame(6.7f)
        };
        private static readonly Frame[] Crafting = {
            new Frame(0), new Frame(.55f, x:2, y:-2, roll:-3),
            new Frame(.8f, y:1, roll:2, prop:1), new Frame(1.05f, x:2, y:-2, roll:-3, prop:-1),
            new Frame(1.35f, y:1, roll:2, prop:1), new Frame(1.65f, x:2, y:-2, roll:-3, prop:-1),
            new Frame(2.1f, y:2, pose:3), new Frame(3.25f, y:2, pose:3), new Frame(4.9f)
        };
        private static readonly Frame[] Walking = {
            new Frame(0), new Frame(.3f, y:-1, width:1.01f, height:.99f),
            new Frame(.7f, x:5, y:4, roll:-2), new Frame(1.1f, x:10),
            new Frame(1.5f, x:15, y:4, roll:2), new Frame(1.9f, x:20),
            new Frame(3.1f, x:20, roll:-4, pose:3), new Frame(4f, x:20),
            new Frame(4.4f, x:15, y:4, roll:2), new Frame(4.8f, x:10),
            new Frame(5.2f, x:5, y:4, roll:-2), new Frame(5.6f), new Frame(7.2f)
        };
        private static readonly Frame[] Tea = {
            new Frame(0), new Frame(.65f, y:-1, roll:3), new Frame(1.1f, y:1, roll:-2, prop:5),
            new Frame(2.1f, y:1, roll:-2, pose:1, prop:5), new Frame(2.65f, y:-1, roll:2, prop:1),
            new Frame(3.2f, pose:2), new Frame(4.2f, pose:2), new Frame(6.6f)
        };
        private static readonly Frame[] Resting = {
            new Frame(0), new Frame(.6f, y:-1, roll:1, pose:1),
            new Frame(2.1f, y:-1, roll:1, pose:1), new Frame(2.8f)
        };

        private void Awake()
        {
            _image = GetComponent<Image>();
            _rect = (RectTransform)transform;
            Rebase();
        }

        /// <summary>Use the companion's traits, never a guess about the user's mood or personality.</summary>
        public void SetTemperaments(IReadOnlyList<SpiritTemperament> temperaments)
        {
            int observant = 0, sociable = 0, seen = 0;
            if (temperaments != null) for (int i = 0; i < temperaments.Count; i++)
            {
                var trait = temperaments[i];
                int value = (int)trait;
                if (value <= 0 || value > (int)SpiritTemperament.Independent || (seen & (1 << value)) != 0) continue;
                seen |= 1 << value;
                switch (trait)
                {
                    case SpiritTemperament.Curious:
                    case SpiritTemperament.Meticulous:
                    case SpiritTemperament.Cautious: observant++; break;
                    case SpiritTemperament.Active:
                    case SpiritTemperament.Sociable:
                    case SpiritTemperament.Playful: sociable++; break;
                }
            }
            _gestureStyle = observant > sociable ? GestureStyle.Observant :
                sociable > observant ? GestureStyle.Sociable : GestureStyle.Balanced;
            _tempo = _gestureStyle == GestureStyle.Observant ? .88f : _gestureStyle == GestureStyle.Sociable ? 1.06f : 1;
            _movement = _gestureStyle == GestureStyle.Observant ? .82f : 1;
        }

        public void RespondToTouch(string mood)
        {
            Perform(mood == "기쁨" ? "happy" : _gestureStyle == GestureStyle.Observant ? "observe" : "wave");
        }

        /// <summary>Call after explicitly relocating an actor; motion is relative to this authored layout.</summary>
        public void Rebase()
        {
            if (_rect == null) _rect = (RectTransform)transform;
            _origin = _rect.anchoredPosition;
            _baseScale = _rect.localScale;
            _baseRotation = _rect.localRotation;
            _current = Frame.Neutral;
        }

        public void BindShadow(RectTransform shadow, CanvasGroup opacity = null)
        {
            _shadow = shadow;
            _shadowGroup = opacity;
            if (shadow != null) { _shadowScale = shadow.localScale; _shadowOrigin = shadow.anchoredPosition; }
            if (opacity != null) _shadowAlpha = opacity.alpha;
        }

        /// <summary>Bind only the held item (book, tool or cup), never the entire station.</summary>
        public void BindProp(RectTransform prop)
        {
            _prop = prop;
            if (prop == null) return;
            _propOrigin = prop.anchoredPosition;
            _propRotation = prop.localRotation;
        }

        public void Perform(string motion)
        {
            if (string.IsNullOrEmpty(motion)) motion = "idle";
            if (_clock - _lastPerform < .22f) return;
            _motion = motion;
            _actionElapsed = 0;
            _lastPerform = _clock;
        }

        private void Update()
        {
            float dt = Mathf.Min(Time.unscaledDeltaTime, .05f);
            _clock += dt;
            _actionElapsed += dt * _tempo;
            if (_lastActivity != Activity) { _lastActivity = Activity; _activityElapsed = 0; }
            _activityElapsed += dt * _tempo;

            var desired = ActivityFrame();
            if (_motion == "sleep") desired = RestFrame();
            else if (_motion != "idle")
            {
                var clip = Clip(_motion);
                if (_actionElapsed >= clip[clip.Length - 1].At) _motion = "idle";
                else desired = Sample(clip, _actionElapsed);
            }

            desired.X *= _movement;
            desired.Y *= _movement;
            desired.Roll *= _movement;
            desired.Prop *= _movement;
            desired.Width = 1 + (desired.Width - 1) * _movement;
            desired.Height = 1 + (desired.Height - 1) * _movement;

            if (ReduceMotion || ForestUiMotion.ReduceMotion)
            {
                int expression = desired.Pose;
                desired = Frame.Neutral;
                desired.Pose = expression;
                _current = desired;
            }
            else _current = Frame.Blend(_current, desired, 1 - Mathf.Exp(-dt * 24));
            Apply(_current);
        }

        private Frame ActivityFrame()
        {
            if (Activity == "rest") return RestFrame();
            if (Activity == "read" || Activity == "craft" || Activity == "walk" || Activity == "tea")
            {
                var clip = Clip(Activity);
                float duration = clip[clip.Length - 1].At;
                if (_gestureStyle == GestureStyle.Balanced) return Sample(clip, _activityElapsed % duration);
                // Look at the finished work or briefly greet the companion, then let the scene rest.
                // Only the body changes here; held props stay in their authored neutral position.
                var pauseGesture = _gestureStyle == GestureStyle.Observant ? Observation : SocialGreeting;
                float gestureDuration = pauseGesture[pauseGesture.Length - 1].At;
                float cycle = _activityElapsed % (duration + .8f + gestureDuration + 2.4f);
                if (cycle < duration) return Sample(clip, cycle);
                if (cycle >= duration + .8f && cycle < duration + .8f + gestureDuration)
                    return Sample(pauseGesture, cycle - duration - .8f);
                return Frame.Neutral;
            }
            float idleTime = _clock * _tempo;
            if (_gestureStyle != GestureStyle.Balanced && idleTime % 10 > 7.9f)
                return Sample(_gestureStyle == GestureStyle.Observant ? Observation : SocialGreeting, idleTime % 10 - 7.9f);
            float breath = Mathf.Sin(idleTime * Mathf.PI * 2 / 4.8f);
            return new Frame(0, y:breath * 1.1f, width:1 - breath * .002f, height:1 + breath * .004f,
                pose:_clock % 5.3f > 5.17f ? 1 : 0);
        }

        private Frame RestFrame()
        {
            float breath = Mathf.Sin(_clock * _tempo * Mathf.PI * 2 / 5.4f);
            return new Frame(0, y:-1 + breath * .45f, roll:1, width:1 - breath * .003f, height:1 + breath * .005f, pose:1);
        }

        private Frame[] Clip(string motion)
        {
            switch (motion)
            {
                case "happy": return _gestureStyle == GestureStyle.Observant ? QuietJoy : Joy;
                case "observe": return Observation;
                case "curious": return Curiosity;
                case "read": return Reading;
                case "craft": return Crafting;
                case "walk": return Walking;
                case "tea": return Tea;
                case "rest": return Resting;
                default: return _gestureStyle == GestureStyle.Observant ? Observation :
                    _gestureStyle == GestureStyle.Sociable ? SocialGreeting : Greeting;
            }
        }

        private static Frame Sample(Frame[] clip, float elapsed)
        {
            for (int i = 1; i < clip.Length; i++)
            {
                if (elapsed > clip[i].At) continue;
                var a = clip[i - 1];
                var b = clip[i];
                float t = Mathf.InverseLerp(a.At, b.At, elapsed);
                var result = Frame.Blend(a, b, t * t * (3 - 2 * t));
                result.Pose = t < .5f ? a.Pose : b.Pose;
                return result;
            }
            return clip[clip.Length - 1];
        }

        private void Apply(Frame frame)
        {
            if (ReadingPose != null && Activity == "read" && _motion == "idle")
                _image.sprite = ReadingPose;
            else if (Poses != null && Poses.Length > frame.Pose && Poses[frame.Pose] != null)
                _image.sprite = Poses[frame.Pose];
            float unit = Mathf.Clamp(_rect.rect.height / 180f, .6f, 1.6f);
            var scale = Vector3.Scale(_baseScale, new Vector3(frame.Width, frame.Height, 1));
            var rotation = _baseRotation * Quaternion.Euler(0, 0, frame.Roll);
            // Keep the lower body planted even when the UI uses a top-left pivot.
            var foot = new Vector3((.5f - _rect.pivot.x) * _rect.rect.width, (.14f - _rect.pivot.y) * _rect.rect.height, 0);
            var compensation = _baseRotation * Vector3.Scale(foot, _baseScale) - rotation * Vector3.Scale(foot, scale);
            _rect.anchoredPosition = _origin + new Vector2(frame.X * unit + compensation.x, frame.Y * unit + compensation.y);
            _rect.localScale = scale;
            _rect.localRotation = rotation;
            if (_shadow != null)
            {
                _shadow.anchoredPosition = _shadowOrigin + Vector2.right * (frame.X * unit);
                float lift = Mathf.Clamp01(Mathf.Max(0, frame.Y) / 18);
                _shadow.localScale = _shadowScale * Mathf.Lerp(1, .86f, lift);
                if (_shadowGroup != null) _shadowGroup.alpha = _shadowAlpha * Mathf.Lerp(1, .65f, lift);
            }
            if (_prop != null)
            {
                // The walking bag travels with the spirit; props on the desk keep their station.
                float carriedX = Activity == "walk" ? frame.X * unit : 0;
                _prop.anchoredPosition = _propOrigin + new Vector2(carriedX, frame.Prop * unit);
                _prop.localRotation = _propRotation * Quaternion.Euler(0, 0, frame.Prop * -.7f);
            }
        }

        private void OnDisable()
        {
            if (_rect == null) return;
            _rect.anchoredPosition = _origin;
            _rect.localScale = _baseScale;
            _rect.localRotation = _baseRotation;
            if (_shadow != null) { _shadow.localScale = _shadowScale; _shadow.anchoredPosition = _shadowOrigin; }
            if (_shadowGroup != null) _shadowGroup.alpha = _shadowAlpha;
            if (_prop != null) { _prop.anchoredPosition = _propOrigin; _prop.localRotation = _propRotation; }
        }
    }
}
