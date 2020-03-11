package org.lcs;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.ReplacementSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends Activity implements TextWatcher, Handler.Callback, View.OnClickListener,ValueAnimator.AnimatorUpdateListener,Animator.AnimatorListener {

	@Override
	public void onAnimationStart(Animator p1) {
		// TODO: Implement this method
	}

	@Override
	public void onAnimationEnd(Animator p1) {
		// TODO: Implement this method
	}

	@Override
	public void onAnimationCancel(Animator p1) {
		// TODO: Implement this method
	}

	@Override
	public void onAnimationRepeat(Animator p1) {
		Editable text;
		MainActivity.ScaleXSpan[] objs;
		currentId++;
		text = a.getText();
		objs = text.getSpans(0, text.length(), ScaleXSpan.class);
		if (objs.length > 0)objs[random.nextInt(objs.length)].setNewId(currentId);
		text = b.getText();
		objs = text.getSpans(0, text.length(), ScaleXSpan.class);
		if (objs.length > 0)objs[random.nextInt(objs.length)].setNewId(currentId);
	}

	class ScaleXSpan extends ReplacementSpan {
		private final String target;
		private int cs,id;

		public ScaleXSpan(String target) {
			this.target = target;
		}
		ScaleXSpan() {id = 0;target = "Hello";}
		@Override
		public int getSize(Paint p, CharSequence t, int s, int e, Paint.FontMetricsInt f) {
			if (id == currentId) {
				float ts=p.measureText(target) * (1.0f - currentScale);
				cs = (int)(p.measureText(t, s, e) * currentScale + ts);
			} else cs = (int) p.measureText(t, s, e);
			return Math.max(1, cs);
		}

		@Override
		public void draw(Canvas c, CharSequence t, int s, int e, float x, int u, int b, int d, Paint p) {
			if (id == currentId) {
				c.save();
				float measureTarget = p.measureText(target);
				float ts=measureTarget * (1.0f - currentScale);
				float measureText = p.measureText(t, s, e);
				float fcs = (measureText * currentScale) + ts;
				if (false) {
					float mid = x + measureTarget * (1.0f - currentScale);
					c.clipRect(x, u, mid, d);
					p.setTextAlign(Paint.Align.RIGHT);
					c.drawText(target, mid, b, p);
					c.restore();
					c.save();
					c.clipRect(mid, u, x + fcs, d);
					p.setTextAlign(Paint.Align.LEFT);
					c.drawText(t, s, e, mid, b, p);
				} else {
					p.setAlpha((int)(currentScale * 255));
					c.clipRect(x, u, x + fcs, d);
					p.setTextAlign(Paint.Align.CENTER);
					c.drawText(t, s, e, x + fcs * 0.5f, b, p);
					p.setAlpha(255 - (int)(currentScale * 255));
					/*c.restore();
					 c.save();
					 c.clipRect(x, u, x + cs, d);*/
					p.setTextAlign(Paint.Align.CENTER);
					c.drawText(target, x + fcs * 0.5f, b, p);
				}
				c.restore();
			} else {
				p.setTextAlign(Paint.Align.LEFT);
				c.drawText(t, s, e, x, b, p);
			}
		}
		public void setNewId(int id) {this.id = id;}
	}
	@Override
	public void onAnimationUpdate(ValueAnimator p) {
		Object o= p.getAnimatedValue();
		if (o instanceof Float) {
			currentScale = (Float)o;
			//setTitle(String.valueOf(o));
			c.setShadowLayer(0, 0, 0, 0);
			b.refreshDrawableState();
		}
	}
	private Random random=new Random();
	private static final int CHECK_PERMISSION = 123;private int currentId=0;
	private float currentScale=1.0f;private int drawCount=0;
	private ValueAnimator animator=new ValueAnimator();
	@Override
	public void onClick(View p1) {
		if (!animator.isRunning()) {
			animator.setDuration(5000);
			//animator.setRepeatMode(animator.RESTART);
			animator.setRepeatCount(-1);
			animator.setFloatValues(new float[]{1.0f,1.0f,0.0f,0.0f,1.0f});
			animator.removeAllUpdateListeners();
			animator.removeAllListeners();
			animator.addUpdateListener(this);
			//animator.addListener(this);
			animator.start();
			Toast.makeText(this, "animator.start()", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onStop() {
		PreferenceManager.getDefaultSharedPreferences(this).edit().putString("a", a.getText().toString()).putString("b", b.getText().toString()).apply();
		super.onStop();
	}

	@Override
	protected void onPause() {
		animator.pause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (animator.isPaused())animator.resume();
		super.onResume();
	}

	private int MODE=0;
	int highLight = 0xfffff000;
	private boolean ready;

	@Override
	public boolean handleMessage(Message m) {
		if (m.what == 234) {
			setTitle(String.valueOf(drawCount));
		} else if (m.what == 123) {
			if (ready) {
				SpannableStringBuilder cc=new SpannableStringBuilder();

				String as = a.getText().toString();
				String bs = b.getText().toString();
				String stringFromJNI = stringFromJNI(as, bs);
				tv.setText(stringFromJNI);
				String[]t=stringFromJNI.split("\2", 2);
				if (t.length == 2) {
					if (MODE == 0) {
						{CharacterStyle[] objs=cc.getSpans(0, cc.length(), CharacterStyle.class);
							for (CharacterStyle i:objs)cc.removeSpan(i);}
						StringBuffer ss;
						String[] ac=t[0].split(" ");
						String[] bc=t[1].split(" ");
						ss = new StringBuffer();
						for (String i:ac)ss.append(i);
						String at=ss.toString();
						ss = new StringBuffer();
						for (String i:bc)ss.append(i);
						String bt=ss.toString();
						if (at.equals(bt)) {
							int af=0,bf=0,be=0,ae=0,ax=0,bx=0,ad,bd;
							while (af < as.length() || bf < bs.length()) {
								if (ae < be) {
									if (ax < ac.length) {
										ad = as.indexOf(ac[ax], af);
										if (ad == -1)break;
										cc.append(as.substring(af, ad), new ScaleXSpan(""), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
										af = ad + ac[ax].length();
										ae += ac[ax++].length();
										if (ae <= be)cc.append(ac[ax - 1], new ForegroundColorSpan(highLight), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
										else cc.append(ac[ax - 1].substring(0, ac[ax - 1].length() + (be - ae)), new ForegroundColorSpan(highLight), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
									} else {
										ad = as.length();
										cc.append(as.substring(af, ad), new ScaleXSpan(""), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
										ae = be;af = ad;
									}
								} else if (ae > be) {
									if (bx < bc.length) {
										bd = bs.indexOf(bc[bx], bf);
										if (bd == -1)break;
										cc.append("\2", new ScaleXSpan(bs.substring(bf, bd)), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
										bf = bd + bc[bx].length();
										be += bc[bx++].length();
										if (ae >= be)
											cc.append(bc[bx - 1], new ForegroundColorSpan(highLight), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
										else cc.append(bc[bx - 1].substring(0, bc[bx - 1].length() + (ae - be)), new ForegroundColorSpan(highLight), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
									} else {
										bd = bs.length();
										cc.append("\2", new ScaleXSpan(bs.substring(bf, bd)), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
										be = ae;bf = bd;
									}
								} else {
									if (ax < ac.length) {
										ad = as.indexOf(ac[ax], af);
										if (ad == -1)break;
									} else ad = as.length();
									if (bx < bc.length) {
										bd = bs.indexOf(bc[bx], bf);
										if (bd == -1)break;
									} else bd = bs.length();
									cc.append(af == ad ?"\2": as.substring(af, ad), new ScaleXSpan(bs.substring(bf, bd)), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

									//bb.setSpan(new ScaleXSpan(as.substring(af, ad)), bf, bd, bf < bd ?Spanned.SPAN_EXCLUSIVE_EXCLUSIVE: Spanned.SPAN_INCLUSIVE_EXCLUSIVE);

									if (ax < ac.length) {
										af = ad + ac[ax].length();
										ae += ac[ax++].length();
									} else af = ad;
									if (bx < bc.length) {
										bf = bd + bc[bx].length();
										be += bc[bx++].length();
									} else bf = bd;
									if (ad < as.length() && bd < bs.length()) cc.append(ae < be ?ac[ax - 1]: bc[bx - 1], new ForegroundColorSpan(highLight), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
								}
							}
						}
						{
							Editable text=a.getText();
							int st=0;int index=0;
							ForegroundColorSpan[] objs=text.getSpans(0, text.length(), ForegroundColorSpan.class);
							for (String i:ac) {
								int nx=TextUtils.indexOf(text, i, st);
								if (nx != -1) {
									st = nx + i.length();
									text.setSpan(index < objs.length ?objs[index++]: new ForegroundColorSpan(highLight), nx, st, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
								}
							}
							while (index < objs.length)text.removeSpan(objs[index++]);
						}
						{
							Editable text=b.getText();
							int st=0;int index=0;
							ForegroundColorSpan[] objs=text.getSpans(0, text.length(), ForegroundColorSpan.class);
							for (String i:bc) {
								int nx=TextUtils.indexOf(text, i, st);
								if (nx != -1) {
									st = nx + i.length();
									text.setSpan(index < objs.length ?objs[index++]: new ForegroundColorSpan(highLight), nx, st, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
								}
							}
							while (index < objs.length)text.removeSpan(objs[index++]);
						}
					}
				}
				c.setText(cc);
			} else tv.setText("library is't ready");
		}
		return true;
	}

	private EditText a, b, c;
	private TextView tv;

	private Handler handler;
	@Override
	public void beforeTextChanged(CharSequence t, int s, int o, int n) {}
	@Override
	public void onTextChanged(CharSequence t, int s, int o, int n) {}
	@Override
	public void afterTextChanged(Editable t) {
		if (ready) {
			handler.removeMessages(123);
			handler.sendEmptyMessageDelayed(123, 1000);
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		tv = new TextView(this);
		a = new EditText(this);
		b = new EditText(this);
		c = new EditText(this);
		SharedPreferences p=PreferenceManager.getDefaultSharedPreferences(this);
		
		a.setText(p.getString("a","hapwithpybirpthildayosophically"));
		b.setText(p.getString("b","happybirthdaywith philosophically"));
		c.setText("init");
		a.setPaintFlags(Paint.LINEAR_TEXT_FLAG);
		b.setPaintFlags(Paint.LINEAR_TEXT_FLAG);
		c.setPaintFlags(Paint.LINEAR_TEXT_FLAG);
		Button t=new Button(this);
		t.setText(android.R.string.autofill);
		t.setOnClickListener(this);
		LinearLayout ll=new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		ll.addView(a);ll.addView(c);ll.addView(b);ll.addView(tv);ll.addView(t);
	  handler = new Handler(this);
		a.addTextChangedListener(this);
		b.addTextChangedListener(this);
		setContentView(ll);
		readyLibrary();
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		if (requestCode == CHECK_PERMISSION)for (int i=0;i < grantResults.length;++i) {
				if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
					readyLibrary();
				}
			}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
	public static void loadLibrary(String fileDir, String real, String name, ArrayList<String>errs) {
		System.load(loadFile(fileDir, real, name, errs, false));
	}
	public static String loadFile(String fileDir, String real, String name, ArrayList<String>errs, boolean executable) {
		Path path=Paths.get("/sdcard/AppProjects/LCS/app/src/main/jniLibs/.build/" + real);
		//String name = "temp";
		Path target=Paths.get(fileDir, "lib" + name + ".so");

		File toFile = target.toFile();
		if (toFile.lastModified() < path.toFile().lastModified()) {
			//errs.add(String.format("%d<%d", toFile.lastModified(), path.toFile().lastModified()));
			try {
				Files.deleteIfExists(target);
				Files.copy(path, target);
			} catch (Exception e) {
				errs.add(e.getLocalizedMessage());
			}
		}
		if (executable != toFile.canExecute()) {
			toFile.setExecutable(executable);
		}
		return target.toString();
	}
	private void readyLibrary() {
		if (PackageManager.PERMISSION_DENIED == checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, CHECK_PERMISSION);
			return;
		}
		ArrayList<String> buf = new ArrayList<String>();
		String filesDir = getFilesDir().toString();
		try{loadLibrary(filesDir, "libhello-jni.so", "helll-jni", buf);}catch(UnsatisfiedLinkError ignore){return;}
		ready = true;
		handler.sendEmptyMessage(123);
	}
	public static native String  stringFromJNI(String a, String b);
}

