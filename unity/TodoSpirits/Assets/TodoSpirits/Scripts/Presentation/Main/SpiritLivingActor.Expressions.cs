using UnityEngine;

namespace TodoSpirits.Presentation.Main
{
    public enum SpiritExpression { Neutral, Happy, Curious, Focused, Sleepy, Surprised }

    public sealed partial class SpiritLivingActor
    {
        private RectTransform _faceArt, _eyeLeft, _eyeRight, _mouth, _browLeft, _browRight;
        private RectTransform _handLeft, _handRight, _footLeft, _footRight;
        private readonly GameObject[] _heldProps = new GameObject[5];
        private RectTransform _heldProp;
        private SpiritExpression _expression;
        public SpiritExpression CurrentExpression => _expression;

        private void BuildExpressionArt()
        {
            if (bodyImage == null || visualRoot == null || _faceArt != null) return;
            var originalFace = bodyImage.transform.Find("Face");
            if (originalFace != null) originalFace.gameObject.SetActive(false);
            _faceArt = new GameObject("ExpressionArt",typeof(RectTransform)).GetComponent<RectTransform>();
            _faceArt.SetParent(bodyImage.transform,false);
            _faceArt.anchorMin=Vector2.zero; _faceArt.anchorMax=Vector2.one; _faceArt.offsetMin=_faceArt.offsetMax=Vector2.zero;
            var ink = new Color(.2f,.32f,.24f);
            _eyeLeft=CompanionWorldArt.Soft(_faceArt,"EyeLeft",new Vector2(.3f,.57f),new Vector2(8,11),ink);
            _eyeRight=CompanionWorldArt.Soft(_faceArt,"EyeRight",new Vector2(.7f,.57f),new Vector2(8,11),ink);
            _mouth=CompanionWorldArt.Soft(_faceArt,"Mouth",new Vector2(.5f,.4f),new Vector2(9,5),ink);
            _browLeft=CompanionWorldArt.Soft(_faceArt,"BrowLeft",new Vector2(.3f,.69f),new Vector2(16,3),ink);
            _browRight=CompanionWorldArt.Soft(_faceArt,"BrowRight",new Vector2(.7f,.69f),new Vector2(16,3),ink);
            CompanionWorldArt.Soft(_faceArt,"CheekLeft",new Vector2(.2f,.43f),new Vector2(15,7),new Color(.91f,.65f,.58f,.6f));
            CompanionWorldArt.Soft(_faceArt,"CheekRight",new Vector2(.8f,.43f),new Vector2(15,7),new Color(.91f,.65f,.58f,.6f));
            var limb=new Color(.58f,.76f,.48f);
            _handLeft=CompanionWorldArt.Soft(visualRoot,"HandLeft",new Vector2(.17f,.34f),new Vector2(22,31),limb);
            _handRight=CompanionWorldArt.Soft(visualRoot,"HandRight",new Vector2(.83f,.34f),new Vector2(22,31),limb);
            _footLeft=CompanionWorldArt.Soft(visualRoot,"FootLeft",new Vector2(.35f,.18f),new Vector2(28,15),limb);
            _footRight=CompanionWorldArt.Soft(visualRoot,"FootRight",new Vector2(.65f,.18f),new Vector2(28,15),limb);
            BuildHeldProps();
            SetExpression(SpiritExpression.Neutral);
        }

        public void SetExpression(SpiritExpression expression)
        {
            if(_faceArt==null) return;
            _expression=expression;
            Vector2 eyes=new Vector2(8,11), mouth=new Vector2(9,5);
            float leftTilt=0, rightTilt=0;
            if(expression==SpiritExpression.Happy) { eyes=new Vector2(13,4); mouth=new Vector2(15,10); leftTilt=20;rightTilt=-20; }
            if(expression==SpiritExpression.Curious) { eyes=new Vector2(10,14); mouth=new Vector2(7,8); }
            if(expression==SpiritExpression.Focused) { eyes=new Vector2(10,6); mouth=new Vector2(8,3); }
            if(expression==SpiritExpression.Sleepy) { eyes=new Vector2(13,3); mouth=new Vector2(6,6); }
            if(expression==SpiritExpression.Surprised) { eyes=new Vector2(12,16); mouth=new Vector2(12,16); }
            _eyeLeft.sizeDelta=eyes; _eyeRight.sizeDelta=eyes;
            _eyeLeft.localRotation=Quaternion.Euler(0,0,leftTilt); _eyeRight.localRotation=Quaternion.Euler(0,0,rightTilt);
            _mouth.sizeDelta=mouth;
            _browLeft.gameObject.SetActive(expression==SpiritExpression.Focused || expression==SpiritExpression.Curious);
            _browRight.gameObject.SetActive(expression==SpiritExpression.Focused);
            _browLeft.localRotation=Quaternion.Euler(0,0,expression==SpiritExpression.Focused?-14:22);
            _browRight.localRotation=Quaternion.Euler(0,0,14);
        }

        private void BuildHeldProps()
        {
            var paper=new Color(.98f,.94f,.81f); var wood=new Color(.54f,.36f,.23f); var green=new Color(.38f,.56f,.34f);
            for(int i=0;i<5;i++)
            {
                var rect=new GameObject("HeldProp"+i,typeof(RectTransform)).GetComponent<RectTransform>();
                rect.SetParent(visualRoot,false); rect.anchorMin=rect.anchorMax=new Vector2(.5f,.28f); rect.sizeDelta=new Vector2(100,65);
                _heldProps[i]=rect.gameObject;
                if(i==0)
                {
                    CompanionWorldArt.Soft(rect,"BookCover",new Vector2(.5f,.5f),new Vector2(92,58),green,.2f);
                    CompanionWorldArt.Soft(rect,"Pages",new Vector2(.5f,.55f),new Vector2(83,50),paper,.2f);
                    CompanionWorldArt.Shape(rect,"Spine",new Vector2(.5f,.55f),new Vector2(3,46),wood);
                    for(int j=0;j<3;j++) CompanionWorldArt.Shape(rect,"Writing"+j,new Vector2(.73f,.35f+j*.16f),new Vector2(25,2),wood);
                }
                else if(i==1)
                {
                    CompanionWorldArt.Soft(rect,"WorkPiece",new Vector2(.25f,.3f),new Vector2(48,25),paper,.15f);
                    CompanionWorldArt.Shape(rect,"HammerGrip",new Vector2(.75f,.6f),new Vector2(9,46),wood);
                    CompanionWorldArt.Soft(rect,"HammerHead",new Vector2(.75f,.96f),new Vector2(37,18),green,.2f);
                }
                else if(i==2)
                {
                    CompanionWorldArt.Shape(rect,"LeafStem",new Vector2(.8f,.5f),new Vector2(4,40),wood);
                    CompanionWorldArt.Soft(rect,"FoundLeaf",new Vector2(.85f,.8f),new Vector2(26,38),green).localRotation=Quaternion.Euler(0,0,-30);
                }
                else if(i==3)
                {
                    CompanionWorldArt.Soft(rect,"Handle",new Vector2(.79f,.5f),new Vector2(28,25),paper);
                    CompanionWorldArt.Soft(rect,"Cup",new Vector2(.5f,.5f),new Vector2(54,42),paper,.25f);
                    CompanionWorldArt.Soft(rect,"Tea",new Vector2(.5f,.79f),new Vector2(44,8),wood);
                }
                else CompanionWorldArt.Soft(rect,"SleepBlanket",new Vector2(.5f,.3f),new Vector2(113,43),new Color(.87f,.66f,.56f),.3f);
                rect.gameObject.SetActive(false);
            }
        }

        private void SetActionArt(TodoSpirits.Core.SpiritActionId action)
        {
            if(_faceArt==null) return;
            for(int i=0;i<_heldProps.Length;i++) _heldProps[i].SetActive(i==(int)action);
            _heldProp=(RectTransform)_heldProps[(int)action].transform;
            _heldProp.anchoredPosition=Vector2.zero; _heldProp.localRotation=Quaternion.identity;
            FaceTravelDirection(Vector2.zero);
            SetExpression(action==TodoSpirits.Core.SpiritActionId.Rest?SpiritExpression.Sleepy:
                action==TodoSpirits.Core.SpiritActionId.SocialTea?SpiritExpression.Happy:
                action==TodoSpirits.Core.SpiritActionId.WalkForest?SpiritExpression.Curious:SpiritExpression.Focused);
        }

        private void FaceTravelDirection(Vector2 direction)
        {
            if(_faceArt==null) return;
            bool back=direction.y>Mathf.Abs(direction.x) && direction.sqrMagnitude>1;
            _faceArt.gameObject.SetActive(!back);
            float side=Mathf.Abs(direction.x)>Mathf.Abs(direction.y)?Mathf.Sign(direction.x):0;
            _faceArt.localScale=new Vector3(side==0?1:.75f,1,1);
            _faceArt.anchoredPosition=new Vector2(side*18,0);
        }

        private void AnimateFeet(float wave)
        {
            if(_footLeft==null) return;
            _footLeft.anchoredPosition=new Vector2(0,wave*5); _footRight.anchoredPosition=new Vector2(0,-wave*5);
        }

        private void AnimateActionArt(TodoSpirits.Core.SpiritActionId action,float time,float wave)
        {
            if(_heldProp==null) return;
            float lift=0, tilt=0;
            if(action==TodoSpirits.Core.SpiritActionId.ReadRecords) tilt=Mathf.Sin(time*.8f)*5;
            if(action==TodoSpirits.Core.SpiritActionId.CraftRepair) { lift=Mathf.Max(0,Mathf.Sin(time*4))*13; tilt=Mathf.Sin(time*4)*12; }
            if(action==TodoSpirits.Core.SpiritActionId.SocialTea) lift=(Mathf.Sin(time*1.2f)+1)*13;
            if(action==TodoSpirits.Core.SpiritActionId.WalkForest)
            {
                AnimateFeet(wave); _faceArt.anchoredPosition=new Vector2(Mathf.Sin(time*.7f)*8,0);
                // A short visual response to the held leaf, without creating a gameplay event or reward.
                var expression=time%12>10.7f && time%12<11.4f?SpiritExpression.Surprised:SpiritExpression.Curious;
                if(_expression!=expression) SetExpression(expression);
            }
            else AnimateFeet(0);
            _heldProp.anchoredPosition=new Vector2(0,lift); _heldProp.localRotation=Quaternion.Euler(0,0,tilt);
            _handLeft.anchoredPosition=new Vector2(0,lift*.5f); _handRight.anchoredPosition=new Vector2(0,lift);
            bool blink=_expression!=SpiritExpression.Sleepy && time%4.5f>4.32f;
            _eyeLeft.localScale=_eyeRight.localScale=new Vector3(1,blink?.15f:1,1);
        }
    }
}
