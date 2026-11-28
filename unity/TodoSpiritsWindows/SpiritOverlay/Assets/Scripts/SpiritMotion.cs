using UnityEngine;

namespace TodoSpirits.Spirit
{
    // Adapted from TodoPetPoC PetPoc.Step: feet position, y-down gravity, .35 restitution.
    public sealed class SpiritMotion
    {
        public Vector2 Feet;
        public Vector2 Velocity;
        public bool Grounded { get; private set; }
        public bool IsEntering { get; private set; }
        private float direction = 1;
        private float entranceElapsed;

        public void BeginEntrance(float width)
        {
            direction = Feet.x <= width / 2f ? 1 : -1;
            entranceElapsed = 0;
            IsEntering = true;
            Release(Vector2.zero);
        }

        public void StepEntrance(float dt, float width, float floor, float size)
        {
            const float peekDuration = .22f;
            const float pauseDuration = .28f;
            const float walkDuration = .9f;
            entranceElapsed += Mathf.Min(dt, .05f);

            float peek = Mathf.Clamp01(entranceElapsed / peekDuration);
            float walk = Mathf.Clamp01((entranceElapsed - peekDuration - pauseDuration) / walkDuration);
            float peekPosition = -.06f * size;
            float landingPosition = Mathf.Min(1.25f * size, width / 2f);
            float distance = Mathf.SmoothStep(-size / 2f, peekPosition, peek)
                + Mathf.SmoothStep(0, landingPosition - peekPosition, walk);
            float stepLift = Mathf.Abs(Mathf.Sin(walk * Mathf.PI * 4)) * size * .04f;

            if (walk >= 1f)
            {
                stepLift = 0;
                IsEntering = false;
                Grounded = true;
            }
            Feet = new Vector2(direction > 0 ? distance : width - distance, floor - stepLift);
        }

        public void Walk(float dt, float width, float size, float scale)
        {
            if (!Grounded || width <= size)
                return;
            float left = size / 2;
            float right = width - left;
            if (Feet.x <= left)
                direction = 1;
            else if (Feet.x >= right)
                direction = -1;
            Feet.x = Mathf.Clamp(Feet.x + direction * 20 * scale * Mathf.Min(dt, .05f), left, right);
        }

        public void Release(Vector2 velocity)
        {
            Velocity = velocity;
            Grounded = false;
        }

        public void Step(float dt, float width, float floor, float size, float scale)
        {
            dt = Mathf.Min(dt, .05f);
            float half = size / 2;
            if (Grounded)
            {
                Feet = new Vector2(Mathf.Clamp(Feet.x, half, width - half), floor);
                return;
            }
            Velocity.y += 2600 * scale * dt;
            Feet += Velocity * dt;
            if (Feet.x < half) { Feet.x = half; Velocity.x = Mathf.Abs(Velocity.x) * .35f; }
            if (Feet.x > width - half) { Feet.x = width - half; Velocity.x = -Mathf.Abs(Velocity.x) * .35f; }
            if (Feet.y < size) { Feet.y = size; Velocity.y = Mathf.Abs(Velocity.y) * .35f; }
            if (Feet.y >= floor)
            {
                Feet.y = floor;
                Velocity = new Vector2(Velocity.x * .55f, -Mathf.Abs(Velocity.y) * .35f);
                if (Mathf.Abs(Velocity.y) < 150 * scale)
                {
                    Velocity = Vector2.zero;
                    Grounded = true;
                }
            }
        }
    }
}
